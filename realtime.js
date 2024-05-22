import { initializeApp} from "https://www.gstatic.com/firebasejs/10.10.0/firebase-app.js";
import { getAnalytics } from "https://www.gstatic.com/firebasejs/10.10.0/firebase-analytics.js";
import { getDatabase, set, get, child, update, ref, remove, onValue} from "https://www.gstatic.com/firebasejs/10.10.0/firebase-database.js";
// Initialize Firebase
const firebaseConfig = {
    apiKey: "AIzaSyC58hQrNqoixKEU2APb9FqnpjNA4PmnwAE",
    authDomain: "fem21app-f43ef.firebaseapp.com",
    databaseURL: "https://fem21app-f43ef-default-rtdb.asia-southeast1.firebasedatabase.app",
    projectId: "fem21app-f43ef",
    storageBucket: "fem21app-f43ef.appspot.com",
    messagingSenderId: "764926693156",
    appId: "1:764926693156:web:216e15768d6394ad6a6db1",
    measurementId: "G-4QTXEQZ53M"
};
// Initialize Firebase app
const app = initializeApp(firebaseConfig);
// Get a reference to the database
const db  = getDatabase(app);

// Reference to the ".info/connected" node from db reference
var connectedRef = ref(db, ".info/connected");
var date = new Date().toISOString().slice(0, 10);
const FOLDER = "FEM21App data/" + date;

//Check whether the website is connected to the database
onValue(connectedRef, (snapshot) => {
    const isConnected = snapshot.val();
    if (isConnected === true) {
        document.querySelector("#status").textContent = "CONNECTED";
        document.getElementById("status").style.color = '#59ec4f';
        console.log("Database is connected");
        // alert("Database is connected");
    } else {
        document.querySelector("#status").textContent = "NOT CONNECTED";
        document.getElementById("status").style.color = 'red';
        console.log("Database is not connected");
    }
});

// Function to register the onValue listener
function registerOnValueListener(folder_name) {

  // Register the new onValue listener
  onValue(ref(db, folder_name + "/STATUS"), (snapshot) => {
    const data = snapshot.val();
    if (data){
      const keys = Object.keys(data);
      keys.forEach((key) => {
        if (key == "HV_STATUS" || key == "BRAKE_SW"){
          const element = document.getElementById(key);
          element.textContent = data[key] ? "ON" : "OFF";
        } 
      })
    } else {
      console.log("No data available at " + folder_name +"/STATUS");
    }
  });

  onValue(ref(db, folder_name + "/TEMPS"), (snapshot) => {
    const data = snapshot.val();
    if (data){
      const keys = Object.keys(data);
      keys.forEach((key) => {
        if (key == "BTR_TEMP" || key == "INV_TEMP"){
          const element = document.getElementById(key);
          element.textContent = data[key];
        } else if (key == "MOTOR_TEMP"){
          const nums = data[key].split('/').map(Number);
          document.getElementById("wheel_1").textContent = nums[0];
          document.getElementById("wheel_2").textContent = nums[1];
          document.getElementById("wheel_3").textContent = nums[2];
          document.getElementById("wheel_4").textContent = nums[3];
        }
      })
    } else {
      console.log("No data available at " + folder_name + "/TEMPS");
    }
  });
}

document.getElementById("configChoose").addEventListener('click', function(){
  var e = document.getElementById("configSelector");
  var value = e.value;
  // var text = e.options[e.selectedIndex].text;
  if (value == "dense"){
    var style = {
      width: "20%",
      height: "400px",
      marginLeft: "0px",
      paddingRight: "10px"
    }
    $(".block").css(style);
    $(".block1").css({
      marginLeft: "0px",
      padding: "0px"
    });
    $("#RUN-choose").css("marginLeft", "0px");
    $(".buttons").css("gap", "1vw");
    $(".pedalChart").css("width", "200px");
    $(".pickers").css({
      flexDirection: "column",
      alignItems: "center",
      width: "fit-content"
    });
    $(".linecharts").css({
      width: "50%",
      float: "right",
      flexDirection: "row",
      alignItems: "center"
    });
    $(".block1").css("width", "");
    console.log("dense");
  } else if (value == "expand") {
    var style = {
      width: "35%",
      height: "400px",
      marginLeft: "100px",
      paddingRight: "0px"
    }
    $(".block").css(style);
    $(".block1").css({
      marginLeft: "70px",
      padding: "30px"
    });
    $("#RUNnumber").css("marginLeft", "0px");
    $(".buttons").css("gap", "1vw");
    $(".pedalChart").css("width", "200px");
    $(".pickers").css({
      flexDirection: "row",
      alignItems: "center"
    });
    $(".linecharts").css({
      width: "100%",
      float: "",
      flexDirection: "column",
      justifyContent: "center"
    });
    $(".linechart").css({
      width: "100%"
    });
    console.log("expand");
  }
  
})

var count = 1;
var folder = "FEM21App data/" + date + "/RUN:" + count;
let stopFlag = 0;
var data_VELOCITY = [];
var data_LV = [];
var data_HV = [];
var data_TORQUE = [[], [], [], []];
var data_ACC = [];
var data_BRAKE = [];
var data_BATTERY_LEVEL = [];

// var VCMINFO = "-";
// var ERROR = "-";
var timestamps_num = 15; //The amount of timestamps
var time_interval = 10; //interval of data reading millisecond
var MaxVelocity = 200;
var MaxLV = 30;
var MaxHV = 600;
var MaxTorque = 200;

// Initial registration of onValue listener
registerOnValueListener(folder);

document.querySelector("#update").addEventListener('click', function(e) {
  e.defaultPrevented;
  get(ref(db, FOLDER))
  .then((snapshot) =>{
      if (snapshot.exists()){
          const info = snapshot.val();
          const KEYS = Object.keys(info);
          const VALUES = KEYS.map(key => info[key]);
          count = VALUES[0];
          document.querySelector("#RUN").textContent = count;
          folder = "FEM21App data/" + date + "/RUN:" + count;
          console.log("COUNT = " + count);
          registerOnValueListener(folder);
      } else {
          alert("COUNT not found");
      }
  }).catch((error)=>{
      alert("UPDATE Error: " + error + " \nCOUNT = " + count + " folder: " + folder);
  });
});

document.getElementById("RUNchoose").addEventListener('click', async function(e){
  count = document.getElementById("RUNnumber").value;
  date = document.getElementById("date").value;
  document.getElementById("RUN").textContent = count;
  folder = "FEM21App data/" + date + "/RUN:" + count;
  console.log("choose:" + count);
  const data = await csvmaker();
  if (data) {
      download(data);
  } else {
      console.log("No data found or error occurred while creating CSV.");
  }
  e.preventDefault;
})

const download = function (data) { 

	// Creating a Blob for having a csv file format 
	// and passing the data with type 
	const blob = new Blob([data], { type: 'text/csv' }); 

	// Creating an object for downloading url 
	const url = window.URL.createObjectURL(blob) 

	// Creating an anchor(a) tag of HTML 
	const a = document.createElement('a') 

	// Passing the blob downloading url 
	a.setAttribute('href', url) 

	// Setting the anchor tag attribute for downloading 
	// and passing the download file name 
  const file_name = date + "/RUN:" + count + ".csv";
	a.setAttribute('download', file_name); 

	// Performing a download with click 
	a.click() 
} 

const csvmaker = async function () { 
  try {
    const snapshot = await get(ref(db, folder));
    if (snapshot.exists()) {
        const Data = snapshot.val();
        var keys = Object.keys(Data);
        const timestamps = Object.keys(Data[keys[0]]);
        // const values = Object.values(Data[keys[0]]);

        
        var csvRows = []
        csvRows.push(keys.join(','));
        //keys = ACC,BATTERY_LEVEL,BRAKE,HV,LV,STATUS,TEMPS,TORQUE,VELOCITY,TIMESTAMPS
        var num = 0;
        for (const timestamp of timestamps){
          const each_row = []
          console.log("timestamp = " + timestamp);
          for (const head of keys){
            // console.log("column = " + head);
            const values = Object.values(Data[head]);
            // console.log("values[0] = " + values[0]);
            each_row.push(values[num]);
          }
          each_row.push(timestamp);
          num++;
          csvRows.push(each_row.join(','));
        }

        // Returning the array joining with new line 
        return csvRows.join('\n');
    }
} catch (error) {
    console.log("CSVMAKER Error!" + error);
}
}


document.getElementsByClassName('picker')[0].addEventListener('change', function(){
  showGraph(this, "VELOCITY");
})
document.getElementsByClassName('picker')[1].addEventListener('change', function(){
  showGraph(this, "LV");
})
document.getElementsByClassName('picker')[2].addEventListener('change', function(){
  showGraph(this, "HV");
})
document.getElementsByClassName('picker')[3].addEventListener('change', function(){
  showGraph(this, "TORQUE");
})

function showGraph(element, category){
  if (element.checked){
    document.getElementById(category).style.display = "";
    timestamps_num = timestamps_num - 5;
  } else{
    document.getElementById(category).style.display = "none";
    timestamps_num = timestamps_num + 5;
  }
}

document.querySelector("#stop").addEventListener('click', function(){
  if(stopFlag == 0){
      stopFlag = 1;
      document.querySelector("#stop").textContent = "CONTINUE";
  } else{
      stopFlag = 0;
      document.querySelector("#stop").textContent = "STOP";
  }
});



function obtainRealtimeData(category){
  if (stopFlag == 0){
    get(ref(db, folder + "/" + category))
    .then((snapshot) =>{
        if (snapshot.exists()){
            const Data = snapshot.val();
            const keys = Object.keys(Data);
            const values = keys.map(key => Data[key]);
            var date = new Date();
            var formattedDate = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}:${date.getMilliseconds().toString().padStart(3, '0')}`;
            // console.log("key:"+ keys[keys.length - 1]);
            // console.log("val:"+ values[values.length - 1]);
            if (category == "VELOCITY"){
              data_VELOCITY.push({
                x: formattedDate,
                y: values[values.length - 1]
              })
              if (data_VELOCITY.length > timestamps_num){
                data_VELOCITY.shift();
              }

            } else if (category == "LV") {
              data_LV.push({
                x: formattedDate,
                y: values[values.length - 1]
              })
              if (data_LV.length > timestamps_num){
                data_LV.shift();
              }

            } else if (category == "HV") {
              data_HV.push({
                x: formattedDate,
                y: values[values.length - 1]
              })
              
              if (data_HV.length > timestamps_num){
                data_HV.shift();
              }
            } else if (category == "TORQUE") {
              let torques = values[values.length - 1].split('/').map(Number);
              for (let i=0; i<4; i++){
                 torques[i] = Math.ceil(torques[i]);
              }
             
              data_TORQUE[0].push({
                x: formattedDate,
                y: torques[0]
              })
              data_TORQUE[1].push({
                x: formattedDate,
                y: torques[1]
              })
              data_TORQUE[2].push({
                x: formattedDate,
                y: torques[2]
              })
              data_TORQUE[3].push({
                x: formattedDate,
                y: torques[3]
              })

              if (data_TORQUE[0].length > timestamps_num){
                data_TORQUE[0].shift();
                data_TORQUE[1].shift();
                data_TORQUE[2].shift();
                data_TORQUE[3].shift();
              }
            
            } else if (category == "ACC"){
              data_ACC.shift();
              data_ACC.push({
                name: 'Acceleration', 
                points: [['value', [0, values[values.length - 1]]]] 
              })

            } else if (category == "BRAKE"){
              data_BRAKE.shift();
              data_BRAKE.push({
                name: 'Brake', 
                points: [['value', [0, values[values.length - 1]]]] 
              })

            } else if (category == "BATTERY_LEVEL"){
              data_BATTERY_LEVEL.shift();
              data_BATTERY_LEVEL.push({
                name: 'Battery', 
                points: [['value', [0, values[values.length - 1]]]] 
              })
            }
        } else {
            stopFlag = 1;
            // alert("No data found, code error:" + stopFlag );
            console.log("ObtainRealtimeData: No data found, code error:" + stopFlag);
            document.querySelector("#stop").textContent = "START";
        }
    }).catch((error)=>{
        console.log("ObtainRealtimeData Error!" + error);
    });
  }
    
}

//for pedal charts
window.setInterval(function() {
  obtainRealtimeData("ACC");
  obtainRealtimeData("BRAKE");
  obtainRealtimeData("BATTERY_LEVEL");
  accChart.options({series: data_ACC});
  brakeChart.options({series: data_BRAKE});
  batteryChart.options({series: data_BATTERY_LEVEL});
}, time_interval);

function getData(category) {
  return get(ref(db, folder + "/" + category))
      .then((snapshot) => {
          if (snapshot.exists()) {
              const Data = snapshot.val();
              const keys = Object.keys(Data);
              const values = keys.map(key => Data[key]);
              return values[values.length - 1];

          } else {
            console.log("getData: No data found");
            throw new Error("No data found");
          }
      })
      .catch((error) => {
          console.log("getData Error:", error);
          throw error; // rethrowing the error for further handling
      });
}

new Vue({
  el: "#VELOCITY",
  components: {
      apexchart: VueApexCharts
  },
  data: {
      series: [{data: data_VELOCITY.slice()}],
      chartOptions: {
      chart: {
          animations: {
          enabled: false,
          easing: "linear",
          dynamicAnimation: {
              speed: 500
          }
          },
          toolbar: {
          show: false
          },
          zoom: {
          enabled: false
          },
      },
      dataLabels: {
          enabled: false
      },
      // stroke: {
      //     curve: "smooth"
      // },

      title: {
          text: "Dynamic Updating Chart: VELOCITY",
          align: "left"
      },
      markers: {
          size:0
      },
      yaxis: {
          max: MaxVelocity,
          min: 0
      },
      legend: {
          show: false
      }
      }
  },
  mounted: function() {
      this.intervals();
  },
  methods: {
      intervals: function() {
          var me = this;
          window.setInterval(function() {
                  obtainRealtimeData("VELOCITY");
                  me.$refs.realtimeChart.updateSeries([{ data: data_VELOCITY }]);
          }, time_interval);
      }
  }
});

new Vue({
  el: "#LV",
  components: {
      apexchart: VueApexCharts
  },
  data: {
      series: [{data: data_LV.slice()}],
      chartOptions: {
      chart: {
          animations: {
          enabled: false,
          easing: "linear",
          dynamicAnimation: {
              speed: 1000
          }
          },
          toolbar: {
          show: false
          },
          zoom: {
          enabled: false
          }
      },
      dataLabels: {
          enabled: false
      },
      // stroke: {
      //     curve: "smooth"
      // },
  
      title: {
          text: "Dynamic Updating Chart: LV",
          align: "left"
      },
      markers: {
          size:0
      },
      yaxis: {
          max: MaxLV,
          min: 0
      },
      legend: {
          show: false
      }
    }
  },
  mounted: function() {
      this.intervals();
  },
  methods: {
      intervals: function() {
          var me = this;
          window.setInterval(function() {
              
                  obtainRealtimeData("LV");
                  // brakeChart.redraw([{animation:false}]);
                  // brakeChart.options({ 
                  //   series: [ 
                  //     { 
                  //       name: 'Accelerator',
                  //       points: [['score', [0, 5]]] 
                  //     }, 
                  //     { 
                  //       name: 'Brake', 
                  //       points: [['score', [0, Math.floor(Math.random() * 101)]]] 
                  //     }
                  //   ] 
                  // })
                  
                  // brakeChart.options.series = [ { points: [['score', [0,Math.floor(Math.random() * 101)]]] } , { points: [['score', [0,Math.floor(Math.random() * 101)]]] } ]; // Update throttle data
                  // brakeChart.options.series = [ { points: [['score', [0,Math.floor(Math.random() * 101)]]] }]; // Update throttle data
              
                  me.$refs.realtimeChart.updateSeries([{ data: data_LV }]);
              
          }, time_interval);
      }
    }
});

new Vue({
  el: "#HV",
  components: {
      apexchart: VueApexCharts
  },
  data: {
      series: [{data: data_HV.slice()}],
      chartOptions: {
      chart: {
          animations: {
          enabled: false,
          easing: "linear",
          dynamicAnimation: {
              speed: 500
          }
          },
          toolbar: {
          show: false
          },
          zoom: {
          enabled: false
          },
      },
      dataLabels: {
          enabled: false
      },
      // stroke: {
      //     curve: "smooth"
      // },

      title: {
          text: "Dynamic Updating Chart: HV",
          align: "left"
      },
      markers: {
          size:0
      },
      yaxis: {
          max: MaxHV,
          min: 0
      },
      legend: {
          show: false
      }
      }
  },
  mounted: function() {
      this.intervals();
  },
  methods: {
      intervals: function() {
          var me = this;
          window.setInterval(function() {
              
                  obtainRealtimeData("HV");
                  me.$refs.realtimeChart.updateSeries([{ data: data_HV }]);
              
          }, time_interval);
      }
  }
});

new Vue({
  el: "#TORQUE",
  components: {
      apexchart: VueApexCharts
  },
  data: {
    series: [],
    // series: [{data: data_TORQUE.slice()}],
    chartOptions: {
      chart: {
          animations: {
          enabled: false,
          easing: "linear",
          dynamicAnimation: {
              speed: 500
          }
          },
          toolbar: {
          show: true
          },
          zoom: {
          enabled: false
          },
      },
      dataLabels: {
          enabled: false
      },
      // stroke: {
      //     curve: "smooth"
      // },

      title: {
          text: "Dynamic Updating Chart: TORQUE",
          align: "left"
      },
      markers: {
          size:0
      },
      yaxis: {
          max: MaxTorque,
          min: 0
      },
      legend: {
          show: true,
          labels: {
            // colors: ['#ff0000', '#00ff00', '#0000ff', '#111111'], // Define colors for each series
            useSeriesColors: true, // Ensure custom colors are used for legend labels
            formatter: function(seriesName, opts) {
              return "<span style='color: " + opts.colors[opts.seriesIndex] + "'>" + seriesName + "</span>";
            }
          }
      }
    }
  },
  mounted: function() {
      this.intervals();
  },
  methods: {
      intervals: function() {
          var me = this;
          window.setInterval(function() {
                  obtainRealtimeData("TORQUE");
                  me.$refs.realtimeChart.updateSeries([
                    { name: "Left-Front" , data: data_TORQUE[0] },
                    { name: "Right-Front" , data: data_TORQUE[1] },
                    { name: "Left-Back" , data: data_TORQUE[2] },
                    { name: "Right-Back" , data: data_TORQUE[3] }
                  ]);
          }, time_interval);
      }
  }
});


ZC.LICENSE = ["569d52cefae586f634c54f86dc99e6a9", "b55b025e438fa8a98e32482b5f768ff5"];
window.feed = function(callback) {
    getData("LV") // Call the getData() function
    .then((data) => {
        var tick = {};
        tick.plot0 = data; // Assign the retrieved data to the plot0 property
        callback(JSON.stringify(tick)); // Pass the data to the callback function
    })
    .catch((error) => {
        console.error("Error fetching data:", error);
        // Handling error - you might want to provide a fallback value or handle the error differently
        var tick = {};
        tick.plot0 = 0; // Assuming a default value if data fetching fails
        callback(JSON.stringify(tick)); // Pass the default value to the callback function
    });
};

var pedalConfig = {
  "graphset": [{
    "type": "bar",
    "background-color": "white",
    "title": {
      "text": "Pedal Bars",
      "font-color": "#7E7E7E",
      "backgroundColor": "none",
      "font-size": "22px",
      "alpha": 1,
      "adjust-layout": true,
    },
    "plotarea": {
      "margin": "dynamic"
    },
    "legend": {
      "layout": "x3",
      "overflow": "page",
      "alpha": 0.05,
      "shadow": false,
      "align": "center",
      "adjust-layout": true,
      "marker": {
        "type": "circle",
        "border-color": "none",
        "size": "10px"
      },
      "border-width": 0,
      "maxItems": 3,
      "toggle-action": "hide",
      "pageOn": {
        "backgroundColor": "#000",
        "size": "10px",
        "alpha": 0.65
      },
      "pageOff": {
        "backgroundColor": "#7E7E7E",
        "size": "10px",
        "alpha": 0.65
      },
      "pageStatus": {
        "color": "black"
      }
    },
    "plot": {
      "bars-space-left": 0.15,
      "bars-space-right": 0.15,
      // "animation": {
      //   "effect": "ANIMATION_SLIDE_BOTTOM",
      //   "sequence": 0,
      //   "speed": 2000,
      //   "delay": 10
      // }
    },
    "scale-y": {
      "line-color": "#7E7E7E",
      "item": {
        "font-color": "#7e7e7e"
      },
      "values": "0:100:10",
      "guide": {
        "visible": true
      },
      "label": {
        "text": "Percentage",
        "font-family": "arial",
        "bold": true,
        "font-size": "30px",
        "font-color": "#7E7E7E",
      },
    },
    "scaleX": {
      "values": ["Pedals"],
      "placement": "default",
      "tick": {
        "size": 20,
      },
      "item": {
        "offsetY": -15
      }
    },
    "tooltip": {
      "visible": false
    },
    "crosshair-x": {
      "line-width": "100%",
      "alpha": 0.18,
      "plot-label": {
        "header-text": "%kv Sales"
      }
    },
    refresh: {
      type: "feed",
      transport: "js",
      url: "feed()",
      interval: 200,
      resetTimeout: 10000
    },
    series: [{
      values: [0],
      backgroundColor: 'black',
    }]
  }]
};

var gaugeConfig = {
  type: "gauge",
  globals: {
    fontSize: 25 //Label size 
  },
  plotarea: {
    marginTop: 80
  },
  plot: {
    size: '100%',
    valueBox: {
      placement: 'center',
      text: '%v', //default
      fontSize: 35, //Size of number shown in the center
      rules: [{
          rule: '%v >= 80',
          text: '%v<br>DANGER'
        },
        {
          rule: '%v < 80 && %v > 50',
          text: '%v<br>FAST'
        },
        {
          rule: '%v <= 50 && %v > 30',
          text: '%v<br>MEDIUM'
        },
        {
          rule: '%v <=  30',
          text: '%v<br>SLOW'
        }
      ]
    }
  },
  tooltip: {
    borderRadius: 5
  },
  scaleR: {
    aperture: 180,
    minValue: 0,
    maxValue: 100,
    step: 10,
    center: {
      visible: false
    },
    tick: {
      visible: true
    },
    item: {
      offsetR: 0,
      rules: [{
        rule: '%i == 9',
        offsetX: 15
      }]
    },
    labels: ['0', '10', '20', '30', '40', '50', '60', '70', '80', '90', '100'],
    ring: {
      size: 50,
      rules: [{
          rule: '%v <= 20',
          backgroundColor: '#29B6F6'
        },
        {
          rule: '%v > 20 && %v < 50',
          backgroundColor: '#FFA726'
        },
        {
          rule: '%v >= 50 && %v <script 80',
          backgroundColor: '#EF5350'
        },
        {
          rule: '%v >= 80',
          backgroundColor: '#E53935'
        }
      ]
    }
  },
  refresh: {
    type: "feed",
    transport: "js",
    url: "feed()",
    interval: 200,
    resetTimeout: 300
  },
  series: [{
    values: [0], // starting value
    backgroundColor: 'black',
    indicator: [10, 10, 10, 10, 0.75],
    animation: {
      effect: 2,
      method: 1,
      sequence: 4,
      speed: 900
    },
  }]
};

zingchart.render({
  id: 'guageChart',
  data: gaugeConfig,
  height: 500,
  width: '100%'
});

var accOption = {
  debug: true, 
  defaultSeries_type: 'gauge linear vertical ', 
  yAxis: { 
    defaultTick_enabled: true, 
    customTicks: [0, 20, 40, 60, 80, 100], 
    scale: { range: [0, 100] }, 
    line: { 
      width: 5, 
      color: 'smartPalette', 
      breaks_gap: 0.03 
    } 
  },
  palette: { 
    pointValue: '%yValue',
    ranges: [ 
        { value: 0, color: '#FCA70F' },
        { value: 20, color: '#FC890F'}, 
        { value: 40, color: '#FC600F' }, 
        { value: 60, color:  '#FF0F17' }, 
        { value: [80,100], color:  '#EE0000'},
    ] 
  }, 
  defaultSeries: {
    defaultPoint_tooltip: '<b>%seriesName percentage:</b> %yValue',
    series_visible: true,
    shape_label: [{
      text: '%value%', // Access value and concatenate with empty string
      verticalAlign: 'bottom',
      style_fontSize: 15
    },
    {
      text: '%name', // Access value and concatenate with empty string
      verticalAlign: 'top',
      style_fontSize: 15
    }]
  }
};

var accChart = new JSC.chart('accChart', accOption);

var brakeOption = accOption;
brakeOption['palette'] = {
  pointValue: '%yValue',
    ranges: [ 
        { value: 0, color: '#E6FF00' },
        { value: 20, color: '#ACFF00'}, 
        { value: 40, color: '#00FF7D' }, 
        { value: 60, color:  '#3CFF00' }, 
        { value: [80,100], color:  '#00FC26'}
    ] 
};

var brakeChart = new JSC.chart('brakeChart', brakeOption);

var batteryOption = brakeOption;
batteryOption['palette'] = {
  pointValue: '%yValue',
  ranges: [ 
          { value: 0, color: '#FF5321' },
          { value: 20, color: '#FF5353'}, 
          { value: 40, color: '#FFD221' }, 
          { value: 60, color:  '#77E6B4' }, 
          { value: [80,100] , color: '#21D683' }
        ] 
};
var batteryChart = new JSC.chart('batteryChart', batteryOption);

// var brakeChart = new JSC.chart('brakeChart', { 
//   debug: true, 
//   defaultSeries_type: 'gauge linear vertical ', 
//   yAxis: { 
//     defaultTick_enabled: true, 
//     customTicks: [0, 20, 40, 60, 80, 100], 
//     scale: { range: [0, 100] }, 
//     line: { 
//       width: 5, 
//       color: 'smartPalette', 
//       breaks_gap: 0.03 
//     } 
//   }, 
//   legend_visible: true, 
//   palette: { 
//     pointValue: '%yValue', 
//     ranges: [ 
//       // { value: 0, color: '#21D683' },
//       // { value: 20, color: '#77E6B4'}, 
//       // { value: 40, color: '#FFD221' }, 
//       // { value: 60, color:  '#FF5353' }, 
//       { value: [0,100], color:  '#FF5321'},
//       // { value: 100, color: '#FF5321' }
//     ] 
//   }, 
//   series: [ 
//     { 
//       name: 'Accelerator', 
//       points: [['score', [0, 15]]] 
//     }, 
//     { 
//       name: 'Brake', 
//       points: [['score', [0, 35]]] 
//     }
//   ] 
// }); 


// Define a function to update the chart data
// function updateChartData() {
//   // Generate random data for the chart
//   var throttleValue = 80; // Random value between 0 and 100 for throttle
//   var brakeValue = Math.floor(Math.random() * 101); // Random value between 0 and 100 for brake
//   // Update the chart with the new data
//   brakeChart.options.series = [ { points: [['score', [0,throttleValue]]] } , { points: [['score', [0,brakeValue]]] } ]; // Update throttle data
//   // brakeChart.options.series[0].points[0] = ['score', [0,throttleValue]];
//   // brakeChart.options.series[0].points[0][1][1] = throttleValue;
//   console.log("value = " + brakeChart.options.series[0].points[0][1][1]);
//   brakeChart.redraw();
// }
// setInterval(updateChartData(), 1000);

