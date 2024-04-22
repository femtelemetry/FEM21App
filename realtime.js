import { initializeApp} from "https://www.gstatic.com/firebasejs/10.10.0/firebase-app.js";
import { getAnalytics } from "https://www.gstatic.com/firebasejs/10.10.0/firebase-analytics.js";
import { getDatabase, set, get, child, update, ref, remove, onValue } from "https://www.gstatic.com/firebasejs/10.10.0/firebase-database.js";
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
// console.log(folder);

//Check whether the website is connected to the database
onValue(connectedRef, (snapshot) => {
    const isConnected = snapshot.val();
    if (isConnected === true) {
        document.querySelector("#status").textContent = "CONNECTED";
        document.getElementById("status").style.color = 'green';
        console.log("Database is connected");
        // alert("Database is connected");
    } else {
        document.querySelector("#status").textContent = "NOT CONNECTED";
        document.getElementById("status").style.color = 'red';
        console.log("Database is not connected");
    }
});
// var data = [];

// Listen for real-time changes in "FEM21App data" in the database
// onValue(ref(db, FOLDER), (snapshot) => {
//     const values = snapshot.val();
//     const keys = Object.keys(values);
//     const value = keys.map(key => values[key]);
//     document.querySelector("#monitor").textContent = value[value.length - 1];
// });

var count = 1;
var folder = "FEM21App data/" + date + "/RUN:" + count;

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
      } else {
          alert("COUNT not found");
      }
  }).catch((error)=>{
      alert("UPDATE Error! COUNT = " + count + "folder:" + folder);
  });
});

document.getElementById("choose").addEventListener('click', function(e){
  count = document.getElementById("RUN-choose").value;
  document.getElementById("RUN").textContent = count;
  folder = "FEM21App data/" + date + "/RUN:" + count;
  console.log("choose:" + count);
  e.preventDefault;
})

document.getElementsByClassName('picker')[0].addEventListener('change', function(){
  showGraph(this, "INVERTER");
})
document.getElementsByClassName('picker')[1].addEventListener('change', function(){
  showGraph(this, "MOTOR");
})
document.getElementsByClassName('picker')[2].addEventListener('change', function(){
  showGraph(this, "VCM");
})
document.getElementsByClassName('picker')[3].addEventListener('change', function(){
  showGraph(this, "BATTERY");
})

function showGraph(element, category){
  if (element.checked){
    document.getElementById(category).style.display = "";
  } else{
    document.getElementById(category).style.display = "none";
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

let stopFlag = 0;
var data_INVERTER = [];
var data_MOTOR = [];
function obtainData(category){
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
            if (category == "INVERTER"){
              data_INVERTER.push({
                x: formattedDate,
                y: values[values.length - 1]
              })
              if (data_INVERTER.length > 20){
                data_INVERTER.shift();
              }
            } else if (category == "MOTOR") {
              data_MOTOR.push({
                x: formattedDate,
                y: values[values.length - 1]
              })
              if (data_MOTOR.length > 20){
                data_MOTOR.shift();
              }
            }
        } else {
            stopFlag = 1;
            // alert("No data found, code error:" + stopFlag );
            document.querySelector("#stop").textContent = "START";
        }
    }).catch((error)=>{
        console.log("ObtainData Error!" + error)
    });
}
function getData(category) {
  return get(ref(db, folder + "/" + category))
      .then((snapshot) => {
          if (snapshot.exists()) {
              const Data = snapshot.val();
              const keys = Object.keys(Data);
              const values = keys.map(key => Data[key]);
              return values[values.length - 1];

          } else {
            console.log("No data found");
            throw new Error("No data found");
          }
      })
      .catch((error) => {
          console.log("getData Error:", error);
          throw error; // rethrowing the error for further handling
      });
}

new Vue({
el: "#INVERTER",
components: {
    apexchart: VueApexCharts
},
data: {
    series: [{data: data_INVERTER.slice()}],
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
        enabled: true
    },
    stroke: {
        curve: "smooth"
    },

    title: {
        text: "Dynamic Updating Chart: INVERTER",
        align: "left"
    },
    markers: {
        size:0
    },
    yaxis: {
        max: 120,
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
            if (stopFlag == 0){
                obtainData("INVERTER");
                me.$refs.realtimeChart.updateSeries([{ data: data_INVERTER }]);
            }
        }, 200);
    }
}});

new Vue({
  el: "#MOTOR",
  components: {
      apexchart: VueApexCharts
  },
  data: {
      series: [{data: data_MOTOR.slice()}],
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
          enabled: true
      },
      stroke: {
          curve: "smooth"
      },
  
      title: {
          text: "Dynamic Updating Chart: MOTOR",
          align: "left"
      },
      markers: {
          size:0
      },
      yaxis: {
          max: 120,
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
              if (stopFlag == 0){
                  obtainData("MOTOR");
                  me.$refs.realtimeChart.updateSeries([{ data: data_MOTOR }]);
              }
          }, 200);
      }
    }
  }
);


ZC.LICENSE = ["569d52cefae586f634c54f86dc99e6a9", "b55b025e438fa8a98e32482b5f768ff5"];
window.feed = function(callback) {
    getData("MOTOR") // Call the getData() function
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

var myConfig = {
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
  data: myConfig,
  height: 500,
  width: '100%'
});


