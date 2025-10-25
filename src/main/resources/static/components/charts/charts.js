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
  }

}