var totalNegative = 0;
var totalPositive = 0;

function getPositives() {
    return totalPositive;
}

function getNegatives(){
    return totalNegative;
}

function getTotais(){
    return totalPositive + totalNegative;
}

function votePositive(message) {
  totalPositive = totalPositive+1;
  drawChart(totalPositive,totalNegative);
  addMessage(message,"POSITIVE");
}

function voteNegative(message) {
  totalNegative = totalNegative+1;
  drawChart(totalPositive,totalNegative);
  addMessage(message,"NEGATIVE");
}

function drawChart(positive, negative) {
  nulo = 0;
  if(positive + negative == 0) {
    nulo = 1;
  }

  var data = google.visualization.arrayToDataTable([
    ['Opnião', 'Total de votos'],
    ['Sem votos', nulo],
    ['Gostou (' + totalPositive + ')', positive],
    ['Nem tanto (' + totalNegative +')', negative]
  ]);

  var options = {
    backgroundColor: 'transparent',
    'legend':'bottom',
    pieHole: 0.4,
    colors: ['#3F51B5','#4CAF50', '#F44336']
  };

  var chart = new google.visualization.PieChart(document.getElementById('piechart_3d'));
  chart.draw(data, options);
}

function addMessage(message, type) {
  var status = "";
  if(type == "NEGATIVE") {
    status = "negative";
  } else if(type == "POSITIVE" ) {
    status = "positive";
  }
  var card = "<div class=\"card "+status+"\"> " + message +" </div>"

  var divMessages = document.getElementById('comments');
  divMessages.insertAdjacentHTML('afterbegin', card);
}
