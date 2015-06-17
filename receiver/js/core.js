var totalNegative = 0;
var totalPositive = 0;

function votePositive() {
  totalPositive = totalPositive+1;
  drawChart(totalPositive,totalNegative);
  addMessage("Oh loco bicho!","POSITIVE");
}

function voteNegative() {
  totalNegative = totalNegative+1;
  drawChart(totalPositive,totalNegative);
  addMessage("Ahhhh mais ou menos","NEGATIVE");
}

function drawChart(positive, negative) {
  nulo = 0;
  if(positive + negative == 0){
    nulo = 1;
  }

  var data = google.visualization.arrayToDataTable([
    ['Opnião', 'Total de votos'],
    ['Sem votos', nulo],
    ['Gostou (' + totalPositive + ')', positive],
    ['Nem tanto (' + totalNegative +')', negative]
  ]);

  var options = {
    'legend':'bottom',
    pieHole: 0.4,
    colors: ['#3F51B5','#4CAF50', '#F44336']
  };

  var chart = new google.visualization.PieChart(document.getElementById('piechart_3d'));
  chart.draw(data, options);
}


function addMessage(message, type){
  var color = "blue";
  if(type == "NEGATIVE"){
    color = "red";
  } else if(type == "POSITIVE" ){
    color = "green";
  }

  var card = "<div class=\"card-panel "+color+"\"> <div class=\"card-content white-text\">" + message +"</div></div>";

  var divMessages = document.getElementById('div_messages');
  divMessages.insertAdjacentHTML('afterbegin', card);
}
