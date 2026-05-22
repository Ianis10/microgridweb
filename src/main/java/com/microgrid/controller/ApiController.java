package com.microgrid.controller;

import com.microgrid.model.MicrogridSnapshot;
import com.microgrid.model.MicrogridSystem;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class ApiController {

    private final MicrogridSystem microgridSystem;

    public ApiController(MicrogridSystem microgridSystem) {
        this.microgridSystem = microgridSystem;
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("role") != null;
    }

    private boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute("role");
        return role != null && "ADMIN".equals(role.toString());
    }

    @GetMapping("/api/simulate")
    public ResponseEntity<?> simulate(HttpSession session) {
        if (!isLoggedIn(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        microgridSystem.simulateStep();
        return ResponseEntity.ok(microgridSystem.getDashboardData());
    }

    @GetMapping("/api/dashboard")
    public ResponseEntity<?> dashboard(HttpSession session) {
        if (!isLoggedIn(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok(microgridSystem.getDashboardData());
    }

    @GetMapping("/api/latest")
    public ResponseEntity<?> latest(HttpSession session) {
        if (!isLoggedIn(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        MicrogridSnapshot snapshot = microgridSystem.getLatestSnapshot();
        return ResponseEntity.ok(snapshot);
    }

    @PostMapping("/api/reset")
    public ResponseEntity<?> reset(HttpSession session) {
        if (!isLoggedIn(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        microgridSystem.resetHistory();
        microgridSystem.simulateStep();
        return ResponseEntity.ok(microgridSystem.getDashboardData());
    }

    @GetMapping("/api/history")
    public ResponseEntity<?> history(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        return ResponseEntity.ok(microgridSystem.getHistory());
    }

    @GetMapping("/api/export/csv")
    public ResponseEntity<?> exportCsv(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        List<MicrogridSnapshot> history = microgridSystem.getHistory();

        StringBuilder csv = new StringBuilder();
        csv.append("timestamp;simulatedHour;solarProduction;loadConsumption;batterySoc;batteryPower;gridImport;gridExport;gridExchange;integratedEnergy;systemStatus\n");

        for (MicrogridSnapshot s : history) {
            csv.append(s.getTimestamp()).append(";")
                    .append(s.getSimulatedHour()).append(";")
                    .append(s.getSolarProduction()).append(";")
                    .append(s.getLoadConsumption()).append(";")
                    .append(s.getBatterySoc()).append(";")
                    .append(s.getBatteryPower()).append(";")
                    .append(s.getGridImport()).append(";")
                    .append(s.getGridExport()).append(";")
                    .append(s.getGridExchange()).append(";")
                    .append(s.getIntegratedEnergy()).append(";")
                    .append("\"").append(s.getSystemStatus().replace("\"", "'")).append("\"\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=microgrid-history.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString());
    }

    @GetMapping("/api/export/excel")
    public ResponseEntity<byte[]> exportExcel(HttpSession session) throws Exception {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).build();
        }

        List<MicrogridSnapshot> history = microgridSystem.getHistory();
            if (history.isEmpty()) {
            for (int i = 0; i < 12; i++) {
                microgridSystem.simulateStep();
            }
            history = microgridSystem.getHistory();
}
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        XSSFCellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));

        XSSFCellStyle intStyle = workbook.createCellStyle();
        intStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));

        XSSFSheet dataSheet = workbook.createSheet("Data");
        XSSFRow headerRow = dataSheet.createRow(0);

        String[] columns = {
                "Timestamp",
                "Simulated Hour",
                "Solar Production (kW)",
                "Load Consumption (kW)",
                "Battery SOC (%)",
                "Battery Power (kW)",
                "Grid Import (kW)",
                "Grid Export (kW)",
                "Grid Exchange (kW)",
                "Integrated Energy (kW)",
                "System Status"
        };

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;
        for (MicrogridSnapshot s : history) {
            Row row = dataSheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(String.valueOf(s.getTimestamp()));

            Cell hourCell = row.createCell(1);
            hourCell.setCellValue(s.getSimulatedHour());
            hourCell.setCellStyle(intStyle);

            Cell solarCell = row.createCell(2);
            solarCell.setCellValue(s.getSolarProduction());
            solarCell.setCellStyle(numberStyle);

            Cell loadCell = row.createCell(3);
            loadCell.setCellValue(s.getLoadConsumption());
            loadCell.setCellStyle(numberStyle);

            Cell socCell = row.createCell(4);
            socCell.setCellValue(s.getBatterySoc());
            socCell.setCellStyle(numberStyle);

            Cell batteryPowerCell = row.createCell(5);
            batteryPowerCell.setCellValue(s.getBatteryPower());
            batteryPowerCell.setCellStyle(numberStyle);

            Cell gridImportCell = row.createCell(6);
            gridImportCell.setCellValue(s.getGridImport());
            gridImportCell.setCellStyle(numberStyle);

            Cell gridExportCell = row.createCell(7);
            gridExportCell.setCellValue(s.getGridExport());
            gridExportCell.setCellStyle(numberStyle);

            Cell gridExchangeCell = row.createCell(8);
            gridExchangeCell.setCellValue(s.getGridExchange());
            gridExchangeCell.setCellStyle(numberStyle);

            Cell integratedCell = row.createCell(9);
            integratedCell.setCellValue(s.getIntegratedEnergy());
            integratedCell.setCellStyle(numberStyle);

            row.createCell(10).setCellValue(s.getSystemStatus());
        }

        dataSheet.setColumnWidth(0, 28 * 256);  // Timestamp
        dataSheet.setColumnWidth(1, 18 * 256);  // Simulated Hour
        dataSheet.setColumnWidth(2, 24 * 256);  // Solar
        dataSheet.setColumnWidth(3, 24 * 256);  // Load
        dataSheet.setColumnWidth(4, 20 * 256);  // Battery SOC
        dataSheet.setColumnWidth(5, 22 * 256);  // Battery Power
        dataSheet.setColumnWidth(6, 20 * 256);  // Grid Import
        dataSheet.setColumnWidth(7, 20 * 256);  // Grid Export
        dataSheet.setColumnWidth(8, 22 * 256);  // Grid Exchange
        dataSheet.setColumnWidth(9, 26 * 256);  // Integrated Energy
        dataSheet.setColumnWidth(10, 28 * 256); // Status

        XSSFSheet chartsSheet = workbook.createSheet("Charts");

        for (int i = 0; i < 20; i++) {
            chartsSheet.setColumnWidth(i, 16 * 256);
        }

        
        if (history.size() >= 2) {
    createSolarLoadChart(workbook, dataSheet, chartsSheet, history.size());
    createBatteryChart(workbook, dataSheet, chartsSheet, history.size());
    createGridChart(workbook, dataSheet, chartsSheet, history.size());
}

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=microgrid-report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(out.toByteArray());
    }

    private void createSolarLoadChart(XSSFWorkbook workbook, XSSFSheet dataSheet, XSSFSheet chartsSheet, int historySize) {
        XSSFDrawing drawing = chartsSheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 1, 9, 18);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Solar Production vs Load Consumption");
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Simulated Hour");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Power (kW)");
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFDataSource<Double> hours = XDDFDataSourcesFactory.fromNumericCellRange(
                dataSheet, new CellRangeAddress(1, historySize, 1, 1));

        XDDFNumericalDataSource<Double> solar = XDDFDataSourcesFactory.fromNumericCellRange(
                dataSheet, new CellRangeAddress(1, historySize, 2, 2));

        XDDFNumericalDataSource<Double> load = XDDFDataSourcesFactory.fromNumericCellRange(
                dataSheet, new CellRangeAddress(1, historySize, 3, 3));

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

        XDDFLineChartData.Series solarSeries = (XDDFLineChartData.Series) data.addSeries(hours, solar);
        solarSeries.setTitle("Solar", null);
        solarSeries.setSmooth(false);
        solarSeries.setMarkerStyle(MarkerStyle.CIRCLE);

        XDDFLineChartData.Series loadSeries = (XDDFLineChartData.Series) data.addSeries(hours, load);
        loadSeries.setTitle("Load", null);
        loadSeries.setSmooth(false);
        loadSeries.setMarkerStyle(MarkerStyle.CIRCLE);

        chart.plot(data);
    }

    private void createBatteryChart(XSSFWorkbook workbook, XSSFSheet dataSheet, XSSFSheet chartsSheet, int historySize) {
        XSSFDrawing drawing = chartsSheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 10, 1, 19, 18);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Battery State of Charge");
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Simulated Hour");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("SOC (%)");
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFDataSource<Double> hours = XDDFDataSourcesFactory.fromNumericCellRange(
                dataSheet, new CellRangeAddress(1, historySize, 1, 1));

        XDDFNumericalDataSource<Double> soc = XDDFDataSourcesFactory.fromNumericCellRange(
                dataSheet, new CellRangeAddress(1, historySize, 4, 4));

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

        XDDFLineChartData.Series socSeries = (XDDFLineChartData.Series) data.addSeries(hours, soc);
        socSeries.setTitle("Battery SOC", null);
        socSeries.setSmooth(false);
        socSeries.setMarkerStyle(MarkerStyle.CIRCLE);

        chart.plot(data);
    }

    private void createGridChart(XSSFWorkbook workbook, XSSFSheet dataSheet, XSSFSheet chartsSheet, int historySize) {
        XSSFDrawing drawing = chartsSheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 20, 19, 37);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Grid Exchange");
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Simulated Hour");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("+ Import / - Export (kW)");
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFDataSource<Double> hours = XDDFDataSourcesFactory.fromNumericCellRange(
                dataSheet, new CellRangeAddress(1, historySize, 1, 1));

        XDDFNumericalDataSource<Double> grid = XDDFDataSourcesFactory.fromNumericCellRange(
                dataSheet, new CellRangeAddress(1, historySize, 8, 8));

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

        XDDFLineChartData.Series gridSeries = (XDDFLineChartData.Series) data.addSeries(hours, grid);
        gridSeries.setTitle("Grid Exchange", null);
        gridSeries.setSmooth(false);
        gridSeries.setMarkerStyle(MarkerStyle.CIRCLE);

        chart.plot(data);
    }
}