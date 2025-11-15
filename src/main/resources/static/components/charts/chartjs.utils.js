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
        //label: '',
        data: data,
        borderColor: 'rgba(255,127,127,0.3)',
        backgroundColor: '#00000050',
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
        legend: {
          display: false
        },
        tooltip: {
          enabled: true,
          displayColors: false,
          padding: { top: 15, right: 25, bottom: 15, left: 25 },
          caretPadding: 20,
          titleFont: {
            size: 16            // Größe des Titels
          },
          bodyFont: {
            size: 16            // Größe der Body-Texte
          },
          backgroundColor: "rgb(15,17,17)",
          titleColor: "#ffffff",
          bodyColor: "#9bbec4",
          callbacks: {
            label:  (cntx) => {
              return cntx.raw.msg; //'hellloollll lllladskfjklasf';
            }
          }
        }
      },
      scales: {
        x: {
          type: 'linear', min: 0, max: 23,
          ticks: {
            stepSize: .5,
            color: chartColors.xAxisLabel,
            callback: function (value) {
              return ''; //value.toString().padStart(2, '0') + ':00';
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
            align: 'start',
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
        chart.data.datasets[0].data.forEach((point, i) => {
          const xPos = x.getPixelForValue(point.x);
          const yPos = y.getPixelForValue(point.y);



          const offset = 15;

          // Text in Kästchen
          const text = `${i+1}`//point.label || point.msg;
          //'Ein Zeuge beobachtet den Mercedes...'; //point.label || point.number.toString(); // Text aus Daten
/*          ctx.font = '1.2rem Arial';
          const textMetrics = ctx.measureText(text);
          const textWidth = textMetrics.width;
          const textHeight = 20; // Höhe des Textes/Kästchens

          const padding = 15; // Padding innerhalb des Kästchens*/
          // const boxWidth = textWidth + padding;
          // const boxHeight = textHeight + padding;

          // Rechteck zeichnen
          // ctx.fillStyle = '#182223'; // Hintergrundfarbe des Kästchens
          // ctx.beginPath();
          // ctx.roundRect(
          //     xPos - boxWidth / 2,
          //     yPos - boxHeight / 2,
          //     boxWidth,
          //     boxHeight,
          //     8
          // );
          // ctx.fill();

          ctx.save();

          ctx.beginPath();
          ctx.setLineDash([5, 5]);
          ctx.moveTo(xPos, yPos + point.r / 2 + offset);
          ctx.lineTo(xPos, bottom);
          ctx.strokeStyle = 'rgba(255,127,127,0.9)';
          ctx.lineWidth = 1;
          ctx.stroke();
          ctx.restore();

          ctx.fillStyle = 'rgba(255,127,127,0.76)';
          ctx.font = '1.1rem Arial';
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';
          //ctx.fillText(point.number, xPos, yPos+2);
          ctx.fillText(text, xPos, yPos);


          ctx.fillStyle = 'rgba(136,156,159, 0.75)';
          ctx.font = '1rem Arial';
          ctx.fillText(point.time, xPos, bottom + 25);
        });

      }
    }]
  });
}