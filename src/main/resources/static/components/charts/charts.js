const CHARTS = {
  all: {},
  initCrimesChart: async () => {
    const ctx = $('#crimesChart').get(0).getContext('2d');
    CHARTS.all.crimes = createBarChart(ctx, INSIGHTS.typesOfCrime, 'crimes', true);
  },
  initCrimesTimelineChart: async () => {
    const ctx = $('#crimesTimelineChart').get(0).getContext('2d');
    CHARTS.all.crimesTimeLine = createLineChart(ctx,
        INSIGHTS.totalCrimes.data,
        INSIGHTS.totalCrimes.labels);
  },
  updateCrimesTimelineChart: async (newData, newLabels) => {
    CHARTS.all.crimesTimeLine.data.labels = newLabels;
    CHARTS.all.crimesTimeLine.data.datasets = newData.map((ds, i) => {
      const col = CHART_LINE_COLORS[i % CHART_LINE_COLORS.length];

      return {
        ...CHARTS.all.crimesTimeLine.data.datasets[i],
        borderColor: col || '#000',
        label: ds.label,
        data: ds.data
    }});

    CHARTS.all.crimesTimeLine.update();
  },
  initCrimeTypesChart: async () => {
    const ctx = $('#crimeTypesChart').get(0).getContext('2d');

    CHARTS.all.crimeTypes = createDoughnutChart(ctx, {
      labels: ['Januar', 'Februar', 'März', 'April', 'Mai'],
      data: [12, 19, 8, 14, 22]
    }, 'type of crime');
  },
  initCrimeDetailsTimelineChart: async () => {
    const ctx = $('#crimeDetailsTimelineChart').get(0).getContext('2d');

    CHARTS.all.crimes = createTimelineChart(ctx,  [
            {x: 2, y: 1.2, r: 30, number: 1, msg: '00:20 ein Zeuge beobachtet..'},
            {x: 10.5, y: 1.6, r: 30, number: 2, msg: '00:30  ein weiterer Zeuge meldet..'},
            {x: 18, y: 1, r: 30, number: 3, msg: '00:40 Festnahme durch Polizei...'},
          ]
        );
  },
  updateCrimeDetailsTimelineChart: async (locations) => {

    if (locations && locations.length > 0) {
      let prevDT = 0;
      let min = 12;
      let max = 12;
      let alltimes = [];

      let ran = (min, max) => Math.random() * (max - min) + min;
      let round = (value) => Math.round(value * 2) / 2;


      locations.sort((a, b) => b.unixts - a.unixts );

      CHARTS.all.crimes.data.datasets[0].data = locations.map((l, i) =>  {
        let tm = CHARTS.calcTime(l.unixts);

        if (tm === prevDT) {
          tm += ran(0.2, 0.9);
        } else if (tm < prevDT) {
          tm = prevDT + ran(0.2, 0.9);
        }

        alltimes.push(tm);

        prevDT = tm;

        let ys = [0.75, 1.5]
        //let ys = [0.3, 0.5, 0.9, 1.2, 1.5, 1.8]

        return {
          x: tm,
          y: ys[i % 2],
          r: 30,
          number: 1,
          msg: l.title
        }
      });

      alltimes.sort()
      min = alltimes[0]
      max = alltimes[alltimes.length -1 ]

      min = min - 0.5;
      max = max + 0.5;

      CHARTS.all.crimes.options.scales.x.min = round(min);
      CHARTS.all.crimes.options.scales.x.max = round(max);


      CHARTS.all.crimes.update();
    }

  },
  calcTime: (timestamp) => {
    let d = new Date(timestamp * 1000);
    let h = d.getHours();
    let m = d.getMinutes();
    let mr = Math.round(100/60*m) * 0.1;
    return (h + mr);
  }

}