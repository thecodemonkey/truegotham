let MAP_INSTANCE = null;
let GEO_JSON_LAYER = null;
let {lat, lon} = { lat: 0, lon: 0};
let SELECTED_DISTRICT = null;
let MARKER_ON_MAP = [];
let MAP_VISIBLE = false;

const MAP = {
  view: async () => {
    return await loadHTML('map');
  },
  init: async () => {

    const elementId = 'map';
    const opts = {
      theme: 'stamen-toner', //'stamen-toner-lite',
      center: [51.5136, 7.4653],
      zoom: 13
    };

    // default to Dortmund Innenstadt coordinates if not provided
    const center = opts?.center ?? [51.5136, 7.4653];
    const zoom = opts?.zoom ?? 13;
    const theme = opts?.theme ?? 'stamen-toner-lite';

    lat = center[0];
    lon = center[1];

    MAP_INSTANCE = L.map(elementId).setView(center, zoom);
    //await MAP.flyIN(center, zoom);

    // if theme defines a className for tiles, ensure we have the CSS for it injected
    if (typeof document !== 'undefined') {
      const def = MAP.THEMES[theme];
      if (def.className) {
        const id = 'map-theme-styles';
        if (!document.getElementById(id)) {
          const style = document.createElement('style');
          style.id = id;
          style.innerHTML = `
					/* green tint for tile images */
					.tiles--green-theme {
						filter: brightness(0.6) saturate(1.3) hue-rotate(80deg) contrast(1.05);
					}
					/* make sure tile images stay crisp */
					.tiles--green-theme.leaflet-tile {
						image-rendering: auto;
					}
				`;
          document.head.appendChild(style);
        }
      }
    }

    let tileLayer = L.tileLayer(MAP.THEMES[theme].url, {
      attribution: MAP.THEMES[theme].attribution,
      maxZoom: MAP.THEMES[theme].maxZoom ?? 19,
      // pass through optional className so tile <img> gets it
      className: (MAP.THEMES[theme]).className
    }).addTo(MAP_INSTANCE);

    function setTheme(newTheme) {
      if (tileLayer) {
        try {
          MAP_INSTANCE.removeLayer(tileLayer);
        } catch (e) { /* ignore */
        }
      }
      tileLayer = L.tileLayer(MAP.THEMES[newTheme].url, {
        attribution: MAP.THEMES[newTheme].attribution,
        maxZoom: MAP.THEMES[newTheme].maxZoom ?? 19
      }).addTo(MAP_INSTANCE);
      // ensure correct sizing after tile swap
      setTimeout(() => MAP_INSTANCE.invalidateSize(), 200);
    }

    // Zoom Event Listener hinzufügen
    MAP_INSTANCE.on('zoomend', () => {
      if (DETAILS_MODE === true || !MAP_VISIBLE) return;

      const zoom = MAP_INSTANCE.getZoom();

      // Definiere Zoom-Bereiche für Opacity
      // z.B.: Zoom 10 = volle Opacity, Zoom 15+ = keine Opacity
      let fillOpacity;

      if (zoom <= 12) {
        fillOpacity = 0.95;
      } else {
        fillOpacity = 0.95 * (1 - (zoom - 10) / 9);
        if (fillOpacity < 0.05) fillOpacity = 0.05;
      }

      console.log(`[map] zoom level: ${zoom}, setting fillOpacity to ${fillOpacity}`);

      // Aktualisiere alle Layer
      if (GEO_JSON_LAYER) {
        GEO_JSON_LAYER.eachLayer((layer) => {
          if (SELECTED_DISTRICT === layer.feature.properties?.statistischer_bezirk) {
            layer.bringToFront();
            return;
          }

          const currentStyle = layer.options;
          layer.setStyle({
            opacity: 1,
            weight: 1,
            fillOpacity: fillOpacity,
            color: '#000'
          });

          if (layer._originalStyle) {
            layer._originalStyle.fillOpacity = fillOpacity;
          }
        });
      }
    });

    MAP_INSTANCE.on('click', async (e) => {
      // console.log('[map] map clicked, closing district mode if open');
      await MAP.closeDistrictMode(e);
    });

    return {MAP_INSTANCE, setTheme};
  },
  THEMES: {
    'osm': {
      url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19
    },
    // Stamen Toner: high-contrast black & white
    'stamen-toner': {
      url: 'http://localhost:7171/api/map/tiles/{z}/{x}/{y}.png',
      //url: 'https://tiles.stadiamaps.com/tiles/stamen_toner/{z}/{x}/{y}.png',
      //url: 'https://stamen-tiles.a.ssl.fastly.net/toner/{z}/{x}/{y}.png',
      attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under CC BY 3.0. Data by <a href="http://openstreetmap.org">OpenStreetMap</a> contributors.',
      maxZoom: 20
    },
    // Stamen Toner Lite: lighter monochrome look (good for backgrounds)
    'stamen-toner-lite': {
      url: 'https://tiles.stadiamaps.com/tiles/stamen_toner-lite/{z}/{x}/{y}.png',
      attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under CC BY 3.0. Data by <a href="http://openstreetmap.org">OpenStreetMap</a> contributors.',
      maxZoom: 20
    },
    // Carto Positron (light) - not strictly monochrome but very desaturated
    'carto-positron': {
      url: 'https://cartodb-basemaps-a.global.ssl.fastly.net/light_all/{z}/{x}/{y}.png',
      attribution: '&copy; <a href="https://carto.com/attributions">CARTO</a> &mdash; © OpenStreetMap contributors',
      maxZoom: 19
    },
    'carto-positron-dark': {
      url: 'https://cartodb-basemaps-a.global.ssl.fastly.net/dark_all/{z}/{x}/{y}.png',
      attribution: '&copy; <a href="https://carto.com/attributions">CARTO</a> &mdash; © OpenStreetMap contributors',
      maxZoom: 19
    },
    // Carto Dark (Dark Matter) - dark basemap similar in style to Positron but dark
    'carto-dark': {
      url: 'https://basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png',
      attribution: '&copy; <a href="https://carto.com/attributions">CARTO</a> &mdash; © OpenStreetMap contributors',
      maxZoom: 19
    }
    ,
    // Carto Dark with green tint: uses same dark tiles but adds a CSS class on tiles
    // so we can tint them green via CSS filters for a dark-green appearance.
    'carto-green': {
      url: 'https://basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png',
      attribution: '&copy; <a href="https://carto.com/attributions">CARTO</a> &mdash; © OpenStreetMap contributors',
      maxZoom: 19,
      //className: 'tiles--green-theme'
    }
  },
  show: async () => {
    $('#map').addClass('on');

    await MAP.loadRegions();
    MAP_VISIBLE = true;
  },
  loadRegions: async () => {
    const url = '/data/dortmund.geojson';
    const defaultStyle = {color: '#000', fillColor:'#1b4249', weight: .2, fillOpacity: 0, opacity:0};
    const hoverStyle = {color: '#ff435f', weight: 2, fillOpacity: 0.2};

    try {
      const res = await fetch(url);
      if (!res.ok) {
        throw new Error(`HTTP ${res.status} while fetching ${url}`);
      }
      const geojson = await res.json();

      // Tooltip für den Bezirksnamen
      let nameTooltip = null;

      const layer = L.geoJSON(geojson, {
        style:  {
          color: '#000',
          fillColor:'#1b4249',
          weight: 1.5,
          opacity: 0,
          fillOpacity: 0
        },
        onEachFeature: (feature, layer) => {
          // Speichere den initialen Style für diesen Layer
          layer._originalStyle = {...defaultStyle};

          layer.on({
            mouseover: (e) => {
              if (SELECTED_DISTRICT === feature.properties?.statistischer_bezirk) {
                layer.bringToFront();
                return;
              }

              const hoveredLayer = e.target;
              // Speichere den aktuellen Style vor dem Hover
              hoveredLayer._preHoverStyle = {
                color: hoveredLayer.options.color || hoveredLayer.options.fillColor,
                fillColor: hoveredLayer.options.fillColor,
                weight: hoveredLayer.options.weight,
                fillOpacity: hoveredLayer.options.fillOpacity
              };

              let hstyle = {
                color: hoverStyle.color,
                weight: 1.5
                //fillColor: hoveredLayer._preHoverStyle.fillColor,
                //fillOpacity: 0.25
              };

              hoveredLayer.setStyle(hstyle);
              hoveredLayer.bringToFront();

            },
            mouseout: (e) => {
              if (SELECTED_DISTRICT === feature.properties?.statistischer_bezirk) {
                layer.bringToFront();
                return
              } else if (SELECTED_DISTRICT) {
                const selfeat = MAP.getSelectedFeature();
                console.log('selected feature on mouseout:', selfeat);
                selfeat?.bringToFront();
              }

              const hoveredLayer = e.target;
              // Stelle den Style wieder her, der VOR dem Hover da war
              if (hoveredLayer._preHoverStyle) {
                hoveredLayer.setStyle(hoveredLayer._preHoverStyle);
              }

              // Tooltip entfernen
              if (nameTooltip) {
                MAP_INSTANCE.removeLayer(nameTooltip);
                nameTooltip = null;
              }
            },
            click: async (e) => {
              e.originalEvent.preventDefault();
              e.originalEvent.stopPropagation();
              e.originalEvent.stopImmediatePropagation();
              await  MAP.onDistrictClick(e, feature);
            }
          });
        }
      }).addTo(MAP_INSTANCE);

      GEO_JSON_LAYER = layer;

      try {
        const bounds = layer.getBounds();
        if (bounds.isValid && !bounds.isEmpty()) {
          MAP_INSTANCE.fitBounds(bounds.pad(0.1));
        }
      } catch (e) {
        // ignore
      }
      return layer;
    } catch (err) {
      console.error('[MAP_INSTANCE] loadGeoJson error', err);
      throw err;
    }
  },
  zoomToAddress: async (address) => {
    MARKER_ON_MAP = [];
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
        address)}`;
    const res = await fetch(url,
        {headers: {'User-Agent': 'LeafletDemoApp/1.0'}});
    const data = await res.json();

    let coords = [];

    if (data.length > 0) {
      coords = [data[0].lat, data[0].lon];

      const msg = 'In der Nacht zu Freitag (26. September) fiel der Fahrer eines Mercedes am Ostwall in Dortmund durch sein rasantes und riskantes Fahrverhalten auf. Ein aufmerksamer Zeuge alarmierte die Polizei und verhinderte so möglicherweise einen schweren Verkehrsunfall. Bei der anschließenden Kontrolle des Fahrzeugs kam Überraschendes ans Licht.';
      const customIcon = L.divIcon({
        className: '',
        html: `<div class="point-marker red"></div>`,
        iconSize: [24, 24],
        iconAnchor: [12, 12]
      });

      const m = L.marker(coords, {icon: customIcon})
      .bindTooltip(msg, {direction: 'auto', className: 'custom-tooltip', offset: [0, 0]  })
      .addTo(MAP_INSTANCE);

      MARKER_ON_MAP.push(m)

      return coords;
    } else {
      alert("Adresse nicht gefunden");
    }
  },
  flyOUT: async() => {
    MAP_INSTANCE.flyTo([lat, lon], 12, {
      animate: true,
      duration: 2
    });
  },
  flyIN: async(coords, zoomLevel) => {
    MAP_INSTANCE.flyTo(coords, zoomLevel, {
      animate: true,
      duration: 2 // Dauer in Sekunden
    });
  },
  flyToBounds: async(bounds) => {

    MAP_INSTANCE.setView(bounds.getCenter(), MAP_INSTANCE.getZoom(), {
      animate: true,
      duration: 1
    });

    //setTimeout(() => {
      MAP_INSTANCE.fitBounds(bounds, {
        animate: true,
        duration: 1
      });
    //}, 100)
  },
  updateDistrict: (districtName, level, i) => {
    if (!GEO_JSON_LAYER) {
      console.warn('GEO_JSON_LAYER not initialized');
      return;
    }

    //console.log(`[MAP] updateDistrict ${districtName} -> level ${level}`);


    let col = ['#1c4249', '#1f3c41',  '#283439',  '#2b2f33', '#2f2b2e', '#33282a', '#392122', '#3a1f20', '#3e1c1c', '#3f1a1a'];

    //const color = level < 1 ? '#1b4249' : '#3f1a1a';
    const strokeColor = level < 1 ? '#000' : '#000';

    let lwl = Math.round(level / 10);
    if (lwl > col.length-1) lwl = col.length-1;

    console.log(`[MAP] updateDistrict ${districtName} -> level ${lwl}`);

    const strokeWeight = lwl > 8 ? 1 : 0.5;

    // Durchsuche alle Layer und aktualisiere den passenden Bezirk
    GEO_JSON_LAYER.eachLayer((layer) => {
      const properties = layer.feature?.properties;
      if (properties?.statistischer_bezirk === districtName) {
        const style = {
          color: strokeColor,
          fillColor: col[lwl],
          weight: 1,
          fillOpacity: 1
        };

        layer.setStyle(style);

        layer._originalStyle = style
      }
    });

    //MAP.flyOUT({lat, lon}, 10);
  },
  resetAllDistricts: async () => {
    if(GEO_JSON_LAYER?.getLayers()) {
      for(const layer of GEO_JSON_LAYER.getLayers()){
        layer.setStyle({
          fillOpacity: 0,
          opacity: 0
        });
      }
    }
  },
  restoreAllDistricts: async () => {
    if(GEO_JSON_LAYER?.getLayers()) {
      for(const layer of GEO_JSON_LAYER.getLayers()){
        if(layer._originalStyle) {
          layer.setStyle(layer._originalStyle);
        }
      }
    }
  },
  onDistrictClick: async (e, feature) => {
    if (DETAILS_MODE === true) return;

    const clickedLayer = e.target;
    const bezirksname = feature.properties?.statistischer_bezirk;

    if (SELECTED_DISTRICT === bezirksname) return;

    // Hier kannst du definieren, was beim Klick passieren soll
    console.log('Bezirk geklickt:', bezirksname);
    console.log('Feature properties:', feature);

    const previousFeature = MAP.getSelectedFeature();

    SELECTED_DISTRICT = bezirksname;

    const bounds = clickedLayer.getBounds();
    const center = bounds.getCenter();

    console.log('bounds:', bounds);
    MAP_INSTANCE.flyTo(center, MAP_INSTANCE.getZoom(), {
      animate: true,
      duration: 1
    });
    //await MAP.flyToBounds(bounds);

    //apply selected style
    clickedLayer.setStyle({
      color: '#ff435f',
      weight: 1.5,
      fillOpacity: 0.2
    });
    clickedLayer.bringToFront();

    // restore previous selected feature style
    if (previousFeature?._originalStyle) {
      previousFeature.setStyle(previousFeature._originalStyle);
    }

    LIST_FILTER.districts = [bezirksname];
    LIST_FILTER.page = 0
    await STMTS_LIST.callSearch(LIST_FILTER);

    // update charts
    $('.titleTotalDistrict').text(` ${bezirksname}`);
    $('.titleListDistrict').text(` ${bezirksname}`);

    let distInsights = await API.insightsTotals([bezirksname]);
    await CHARTS.updateCrimesTimelineChart(distInsights.data, distInsights.labels);

    await DASHBOARD.showDistrictImage(bezirksname)
    await delay(100);
    await DASHBOARD.showDistrictDescription(bezirksname);
  },
  closeDistrictMode: async (e) => {
    if(SELECTED_DISTRICT) {

      let insideLayers = await MAP.getLayersByCoords([[e.latlng.lng, e.latlng.lat]]);
      if (insideLayers.length > 0) {
        console.log('[map] clicked inside a district, not closing district mode');
        return;
      }

      SELECTED_DISTRICT = null;
      console.log('[map] clicked OUTSIDE a district, closing district mode!');


      await MAP.restoreAllDistricts();
      await MAP.flyOUT();

      // update charts
      $('.titleTotalDistrict').text(``);
      $('.titleListDistrict').text(``);

      if (INSIGHTS?.totalCrimes){
        await CHARTS.updateCrimesTimelineChart(INSIGHTS.totalCrimes.data, INSIGHTS.totalCrimes.labels);
      }

      // reset statements list filter
      LIST_FILTER.districts = [];
      LIST_FILTER.page = 0
      await STMTS_LIST.callSearch(LIST_FILTER);

      await DASHBOARD.hideDistrictImage();
      await delay(100);
      await DASHBOARD.hideDistrictDescription();
    }
  },
  getSelectedFeature: () => {
    return GEO_JSON_LAYER ? GEO_JSON_LAYER.getLayers().find(layer => {
      return layer.feature?.properties?.statistischer_bezirk === SELECTED_DISTRICT;
    }) : null;
  },
  getLayersByCoords: async (points) => {
    if (!GEO_JSON_LAYER) return [];

    const layers = [];
    GEO_JSON_LAYER.eachLayer((layer) => {

      if (layer.feature?.geometry?.type === "Polygon" || layer.feature?.geometry?.type === "MultiPolygon") {
        for (const point of points) {
          const pt = turf.point(point);
          if (turf.booleanPointInPolygon(pt, layer.feature)) {
            layers.push(layer);
            break;
          }
        }
      }
    });

    return layers;
  },
  showCrimeDetails: async (loc, address) => {
    await MAP.resetAllDistricts();
    const coords = await MAP.zoomToAddress(address);
    const layers = await MAP.getLayersByCoords([coords.reverse()]);

    if (layers?.length > 0) {
      layers[0].setStyle({
        color: '#ff435f',
        opacity: 1,
        weight: 1,
        fillOpacity: 0.05,
        fillColor: '#ff435f'
      });

      await MAP.flyToBounds(layers[0].getBounds());
    }
  },
  closeCrimeDetails: async () => {
    MARKER_ON_MAP?.forEach(m => MAP_INSTANCE.removeLayer(m));

    await MAP.restoreAllDistricts();


    const layer = MAP.getSelectedFeature();
    if (!layer) await MAP.flyOUT();
    await MAP.setLayerStyleSelected(layer);
  },
  setLayerStyleSelected: async (layer) => {
    if (layer) {
      layer.setStyle({
        color: '#ff435f',
        opacity: 1,
        weight: 1.5,
        fillOpacity: 0.2
      });
      layer.bringToFront();
      await MAP.flyToBounds(layer.getBounds());
    }
  }
}