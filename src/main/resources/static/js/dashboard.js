let solarData = [];
let loadData = [];
let batteryData = [];
let gridData = [];
let labels = [];
const maxPoints = 12;
let currentRole = null;

function createCommonOptions(titleText, yAxisText) {
    return {
        responsive: true,
        maintainAspectRatio: false,
        animation: false,

        interaction: {
            mode: "index",
            intersect: false
        },

        plugins: {
            legend: {
                position: "top",
                labels: {
                    color: "#f8fafc",
                    font: {
                        size: 15,
                        weight: "bold"
                    },
                    padding: 20,
                    boxWidth: 26
                }
            },

            title: {
                display: true,
                text: titleText,
                color: "#f8fafc",
                font: {
                    size: 20,
                    weight: "bold"
                },
                padding: {
                    top: 12,
                    bottom: 24
                }
            },

            tooltip: {
                backgroundColor: "rgba(15,23,42,0.95)",
                titleColor: "#f8fafc",
                bodyColor: "#e5e7eb",
                titleFont: {
                    size: 14,
                    weight: "bold"
                },
                bodyFont: {
                    size: 13
                },
                padding: 12
            }
        },

        elements: {
            line: {
                borderWidth: 3,
                tension: 0.35
            },
            point: {
                radius: 4,
                hoverRadius: 7
            }
        },

        scales: {
            x: {
                ticks: {
                    color: "#e5e7eb",
                    font: {
                        size: 13,
                        weight: "bold"
                    },
                    maxRotation: 0,
                    minRotation: 0
                },

                title: {
                    display: true,
                    text: "Simulated Hour",
                    color: "#f8fafc",
                    font: {
                        size: 16,
                        weight: "bold"
                    }
                },

                grid: {
                    color: "rgba(255,255,255,0.12)"
                },

                border: {
                    color: "rgba(255,255,255,0.35)"
                }
            },

            y: {
                beginAtZero: true,

                ticks: {
                    color: "#e5e7eb",
                    font: {
                        size: 13,
                        weight: "bold"
                    }
                },

                title: {
                    display: true,
                    text: yAxisText,
                    color: "#f8fafc",
                    font: {
                        size: 16,
                        weight: "bold"
                    }
                },

                grid: {
                    color: "rgba(255,255,255,0.12)"
                },

                border: {
                    color: "rgba(255,255,255,0.35)"
                }
            }
        }
    };
}

const solarLoadChart = new Chart(document.getElementById("solarLoadChart"), {
    type: "line",
    data: {
        labels: labels,
        datasets: [
            {
                label: "Solar",
                data: solarData,
                borderColor: "#22c55e",
                backgroundColor: "#22c55e",
                fill: false
            },
            {
                label: "Load",
                data: loadData,
                borderColor: "#38bdf8",
                backgroundColor: "#38bdf8",
                fill: false
            }
        ]
    },
    options: createCommonOptions("Solar Production vs Load Consumption", "Power (kW)")
});

const batteryChart = new Chart(document.getElementById("batteryChart"), {
    type: "line",
    data: {
        labels: labels,
        datasets: [
            {
                label: "Battery SOC",
                data: batteryData,
                borderColor: "#a21caf",
                backgroundColor: "#a21caf",
                fill: false
            }
        ]
    },
    options: createCommonOptions("Battery State of Charge", "SOC (%)")
});

const gridChart = new Chart(document.getElementById("gridChart"), {
    type: "line",
    data: {
        labels: labels,
        datasets: [
            {
                label: "Grid Exchange",
                data: gridData,
                borderColor: "#92400e",
                backgroundColor: "#92400e",
                fill: false
            }
        ]
    },
    options: createCommonOptions("Grid Exchange", "+ Import / - Export (kW)")
});

function redirectToLoginIfUnauthorized(res) {
    if (res.status === 401) {
        window.location.href = "/login";
        throw new Error("Unauthorized");
    }
    return res;
}

function signed(v) {
    return (v >= 0 ? "+" : "") + Number(v).toFixed(2);
}

function updateCharts(snapshot) {
    if (labels.length >= maxPoints) {
        labels.shift();
        solarData.shift();
        loadData.shift();
        batteryData.shift();
        gridData.shift();
    }

    labels.push(String(snapshot.simulatedHour).padStart(2, "0") + ":00");
    solarData.push(snapshot.solarProduction);
    loadData.push(snapshot.loadConsumption);
    batteryData.push(snapshot.batterySoc);
    gridData.push(snapshot.gridExchange);

    solarLoadChart.update();
    batteryChart.update();
    gridChart.update();
}

function updateUI(data) {
    const s = data.snapshot;

    document.getElementById("solar").innerText = s.solarProduction.toFixed(2) + " kW";
    document.getElementById("load").innerText = s.loadConsumption.toFixed(2) + " kW";
    document.getElementById("soc").innerText = s.batterySoc.toFixed(2);
    document.getElementById("energy").innerText = signed(s.integratedEnergy) + " kW";
    document.getElementById("grid").innerText = signed(s.gridExchange) + " kW";
    document.getElementById("status").innerText = s.systemStatus;

    const alerts = s.alerts && s.alerts.length ? s.alerts.map(a => "- " + a).join("\n") : "No alerts.";
    document.getElementById("alerts").innerText = alerts;

    let avgText = "";
    avgText += "DAY\n";
    avgText += `Solar: ${data.day.solarAverage.toFixed(2)} kW\n`;
    avgText += `Load: ${data.day.loadAverage.toFixed(2)} kW\n`;
    avgText += `SOC: ${data.day.socAverage.toFixed(2)} %\n`;
    avgText += `Integrated: ${signed(data.day.integratedAverage)} kW\n`;
    avgText += `Grid +/-: ${signed(data.day.gridExchangeAverage)} kW\n\n`;

    avgText += "WEEK\n";
    avgText += `Solar: ${data.week.solarAverage.toFixed(2)} kW\n`;
    avgText += `Load: ${data.week.loadAverage.toFixed(2)} kW\n`;
    avgText += `SOC: ${data.week.socAverage.toFixed(2)} %\n`;
    avgText += `Integrated: ${signed(data.week.integratedAverage)} kW\n`;
    avgText += `Grid +/-: ${signed(data.week.gridExchangeAverage)} kW\n\n`;

    avgText += "MONTH\n";
    avgText += `Solar: ${data.month.solarAverage.toFixed(2)} kW\n`;
    avgText += `Load: ${data.month.loadAverage.toFixed(2)} kW\n`;
    avgText += `SOC: ${data.month.socAverage.toFixed(2)} %\n`;
    avgText += `Integrated: ${signed(data.month.integratedAverage)} kW\n`;
    avgText += `Grid +/-: ${signed(data.month.gridExchangeAverage)} kW\n\n`;

    avgText += "YEAR\n";
    avgText += `Solar: ${data.year.solarAverage.toFixed(2)} kW\n`;
    avgText += `Load: ${data.year.loadAverage.toFixed(2)} kW\n`;
    avgText += `SOC: ${data.year.socAverage.toFixed(2)} %\n`;
    avgText += `Integrated: ${signed(data.year.integratedAverage)} kW\n`;
    avgText += `Grid +/-: ${signed(data.year.gridExchangeAverage)} kW\n`;

    document.getElementById("averages").innerText = avgText;

    updateCharts(s);
}

async function simulate() {
    const res = await fetch("/api/simulate");
    redirectToLoginIfUnauthorized(res);
    const data = await res.json();
    updateUI(data);
}

async function resetAll() {
    const res = await fetch("/api/reset", { method: "POST" });
    redirectToLoginIfUnauthorized(res);
    const data = await res.json();

    labels.length = 0;
    solarData.length = 0;
    loadData.length = 0;
    batteryData.length = 0;
    gridData.length = 0;

    solarLoadChart.update();
    batteryChart.update();
    gridChart.update();

    updateUI(data);
    document.getElementById("historyBox").innerText = currentRole === "ADMIN" ? "History reset." : "Admin only.";
}

async function loadHistory() {
    if (currentRole !== "ADMIN") return;

    const res = await fetch("/api/history");
    if (res.status === 403) {
        document.getElementById("historyBox").innerText = "Forbidden.";
        return;
    }
    redirectToLoginIfUnauthorized(res);
    const history = await res.json();

    const lines = history.slice(-12).map(h =>
        `${String(h.simulatedHour).padStart(2, "0")}:00 | Solar=${h.solarProduction.toFixed(2)} | Load=${h.loadConsumption.toFixed(2)} | SOC=${h.batterySoc.toFixed(2)} | Grid=${signed(h.gridExchange)}`
    );

    document.getElementById("historyBox").innerText = lines.length ? lines.join("\n") : "No history.";
}

function applyColors() {
    if (currentRole !== "ADMIN") return;

    solarLoadChart.data.datasets[0].borderColor = document.getElementById("solarColor").value;
    solarLoadChart.data.datasets[0].backgroundColor = document.getElementById("solarColor").value;

    solarLoadChart.data.datasets[1].borderColor = document.getElementById("loadColor").value;
    solarLoadChart.data.datasets[1].backgroundColor = document.getElementById("loadColor").value;

    batteryChart.data.datasets[0].borderColor = document.getElementById("batteryColor").value;
    batteryChart.data.datasets[0].backgroundColor = document.getElementById("batteryColor").value;

    gridChart.data.datasets[0].borderColor = document.getElementById("gridColor").value;
    gridChart.data.datasets[0].backgroundColor = document.getElementById("gridColor").value;

    solarLoadChart.update();
    batteryChart.update();
    gridChart.update();
}

function exportCsv() {
    if (currentRole !== "ADMIN") return;
    window.open("/api/export/csv", "_blank");
}

function exportExcel() {
    if (currentRole !== "ADMIN") return;
    window.open("/api/export/excel", "_blank");
}

async function logout() {
    await fetch("/api/auth/logout", { method: "POST" });
    window.location.href = "/login";
}

async function loadSessionInfo() {
    const res = await fetch("/api/auth/me");
    if (res.status === 401) {
        window.location.href = "/login";
        return;
    }

    const data = await res.json();
    currentRole = data.role;

    document.getElementById("userInfo").innerText = `${data.username} | ${data.role}`;

    const adminOnly = currentRole === "ADMIN";
    document.getElementById("historyBtn").style.display = adminOnly ? "inline-block" : "none";
    document.getElementById("colorBtn").style.display = adminOnly ? "inline-block" : "none";
    document.getElementById("exportBtn").style.display = adminOnly ? "inline-block" : "none";
    document.getElementById("exportExcelBtn").style.display = adminOnly ? "inline-block" : "none";
    document.getElementById("adminColors").style.display = adminOnly ? "block" : "none";
}

async function init() {
    await loadSessionInfo();

    const res = await fetch("/api/dashboard");
    redirectToLoginIfUnauthorized(res);
    const data = await res.json();
    updateUI(data);
}

init();
setInterval(simulate, 2000);