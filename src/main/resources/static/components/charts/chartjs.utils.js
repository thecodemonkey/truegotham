const chartColors = {
  xAxisLabel: 'rgba(255, 255, 255, 0.28)',
  yAxisLabel: 'rgba(255, 255, 255, 0.28)',
  grid: 'rgba(255,255,255,0.1)',

  barBackground: 'rgb(82,102,103)',
  barBorder: 'rgb(148,217,216)',

  doughnutBackground: ['rgb(105,99,63)'],
  doughnutBorder: 'rgba(0,0,0,.5)'
}
const CHART_LINE_COLORS = [
  '#e74c3c', // Rot
  '#3498db', // Blau
  '#2ecc71', // Grün
  '#f1c40f', // Gelb
  '#9b59b6', // Lila
  '#e67e22', // Orange
  '#1abc9c'  // Türkis
];


function createBarChart(ctx, dataSet, label, isHorizontal) {
  const bgColors = dataSet.data.map((_, index) => index < 1 ? '#ff4f4f' : chartColors.barBackground);

  return new Chart(ctx, {
    type: 'bar',
    data: {
      labels: dataSet.labels, //['Januar', 'Februar', 'März', 'April', 'Mai'],
      datasets: [{
        label: label, //'crimes',
        data: dataSet.data, //[12, 19, 8, 14, 22],
        backgroundColor: bgColors,
        borderColor: chartColors.barBorder,
        borderWidth: 0
      }]
    },
    options: {
      indexAxis: isHorizontal ? 'y' : 'x',
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {display: false},
        title: {display: false}
      },
      scales: {
        x: {
          ticks: {color: chartColors.xAxisLabel}, grid: {
            display: isHorizontal,
            color: chartColors.grid
          }
        },
        y: {
          beginAtZero: true,
          ticks: {
            autoSkip: false,
            color: chartColors.yAxisLabel
          },
          grid: {
            display: !isHorizontal,
            color: chartColors.grid
          }
        }
      }
    }
  });
}

function createDoughnutChart(ctx, dataSet, label) {
  return new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: dataSet.labels, // z.B. ['Red', 'Blue', 'Yellow']
      datasets: [{
        label: label, // z.B. 'Crimes'
        data: dataSet.data, // z.B. [12, 19, 8]
        backgroundColor: chartColors.doughnutBackground, // Array oder einzelne Farbe
        borderColor: chartColors.doughnutBorder,         // Array oder einzelne Farbe
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {display: false},
        title: {display: false}
      }
    }
  });
}

function createLineChart(ctx, dataSets, labels) {



  return new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: dataSets.map((ds, i) => {
        const col = CHART_LINE_COLORS[i % CHART_LINE_COLORS.length];
        return {
          label: ds.label,
          data: ds.data,
          borderColor: col || '#000',
          backgroundColor: ds.backgroundColor || 'transparent',
          borderWidth: 1,
          fill: false,
          tension: 0.3,
          pointRadius: 2,
          pointHoverRadius: 6
        }
      })
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: true,
          position: 'right',
          padding: {
            left: 25
          },
          labels: {
            color: '#ffffffA0',
            usePointStyle: true,
            pointStyle: 'circle',
            boxWidth: 50,
            boxHeight: 5,
            borderRadius: 0
          }
        },
        title: {display: false}
      },
      scales: {
        x: {
          ticks: {color: chartColors.xAxisLabel},
          grid: {display: false}
        },
        y: {
          beginAtZero: true,
          ticks: {color: chartColors.yAxisLabel},
          grid: {color: chartColors.grid}
        }
      }
    }
  });
}

function createTimelineChart(ctx, data) {
  return new Chart(ctx, {
    type: 'bubble',
    data: {
      datasets: [{
        label: 'Uhrzeit',
        data: data,
        borderColor: '#ff000000',
        backgroundColor: '#00000020',
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      onClick: (event, activeElements) => {
        if (activeElements.length > 0) {
          const clickedPoint = event.chart.data.datasets[activeElements[0].datasetIndex].data[activeElements[0].index];
          console.log('Bubble geklickt:', clickedPoint);
        }
      },
      plugins: {
        legend: {display: false},
        tooltip: {
          enabled: false,
          callbacks: {
            label: function (context) {
              return 'hellloollll lllladskfjklasf';
            }
          }
        }
      },
      scales: {
        x: {
          type: 'linear', min: 0, max: 23,
          ticks: {
            stepSize: 1,
            color: chartColors.xAxisLabel,
            callback: function (value) {
              return value.toString().padStart(2, '0') + ':00';
            }
          },
          grid: {
            display: true,
            drawOnChartArea: false,
            drawTicks: true,
            tickLength: 10,
            color: chartColors.xAxisLabel
          },
          border: {
            display: true,
            color: chartColors.xAxisLabel, // oder eine andere Farbe
            width: 1
          },
          title: {
            display: true,
            text: 'Uhrzeit', // oder ein anderer Titel
            color: chartColors.xAxisLabel,
            font: {
              size: 14
            }
          }
        },
        y: {
          min: 0,
          max: 2,
          display: true,
          ticks: {
            display: false
          },
          //ticks: {color: chartColors.yAxisLabel},
          grid: {color: chartColors.grid}
        }
      }
    },
    plugins: [{
      id: 'customCirclesWithLines',
      afterDatasetsDraw(chart) {
        const {ctx, chartArea: {bottom}, scales: {x, y}} = chart;
        chart.data.datasets[0].data.forEach(point => {
          const xPos = x.getPixelForValue(point.x);
          const yPos = y.getPixelForValue(point.y);

          const offset = 15;

          // Text in Kästchen
          const text = point.label || point.msg;
          //'Ein Zeuge beobachtet den Mercedes...'; //point.label || point.number.toString(); // Text aus Daten
          ctx.font = '1.1rem Arial';
          const textMetrics = ctx.measureText(text);
          const textWidth = textMetrics.width;
          const textHeight = 20; // Höhe des Textes/Kästchens

          const padding = 15; // Padding innerhalb des Kästchens
          const boxWidth = textWidth + padding;
          const boxHeight = textHeight + padding;

          // Rechteck zeichnen
          ctx.fillStyle = '#182223'; // Hintergrundfarbe des Kästchens
          ctx.beginPath();
          ctx.roundRect(
              xPos - boxWidth / 2,
              yPos - boxHeight / 2,
              boxWidth,
              boxHeight,
              8
          );
          ctx.fill();

          ctx.save();
          ctx.beginPath();
          ctx.setLineDash([5, 5]);
          ctx.moveTo(xPos, yPos + point.r / 2 + offset);
          ctx.lineTo(xPos, bottom);
          ctx.strokeStyle = '#72fbffA0';
          ctx.lineWidth = 1;
          ctx.stroke();
          ctx.restore();

          ctx.fillStyle = 'rgba(182,207,210,0.63)';
          ctx.font = '.9rem Arial';
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';
          //ctx.fillText(point.number, xPos, yPos+2);
          ctx.fillText(text, xPos, yPos);

        });
      }
    }]
  });
}