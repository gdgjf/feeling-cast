window.onload = function() {
  cast.receiver.logger.setLevelValue(0);
  window.castReceiverManager = cast.receiver.CastReceiverManager.getInstance();
  console.log('Starting Receiver Manager');

    // create a CastMessageBus to handle messages for a custom namespace
    window.messageBus = window.castReceiverManager.getCastMessageBus(
      'urn:x-cast:net.fbvictorhugo.feelingcast');

  // handler for the 'ready' event
  castReceiverManager.onReady = function(event) {
    console.log('Received Ready event: ' + JSON.stringify(event.data));
    window.castReceiverManager.setApplicationState("Application status is ready...");
  };

  // handler for 'senderconnected' event
  castReceiverManager.onSenderConnected = function(event) {
    console.log('Received Sender Connected event: ' + event.data);
    console.log(window.castReceiverManager.getSender(event.data).userAgent);
   
  };

  // handler for 'senderdisconnected' event
  castReceiverManager.onSenderDisconnected = function(event) {
    console.log('Received Sender Disconnected event: ' + event.data);
    if (window.castReceiverManager.getSenders().length == 0) {
      window.close();
    }
  };


  // handler for the CastMessageBus message event
  window.messageBus.onMessage = function(event) {
    console.log('Message [' + event.senderId + ']: ' + event.data);
    var json = JSON.parse(event.data);

    var correct = false;

    if (json.choice == 'positive') {
      votePositive(json.message);
      window.messageBus.broadcast(getPositives() + " positivos de "  + getTotais());
    } else if (json.choice == 'negative') {
      voteNegative(json.message);
      window.messageBus.broadcast(getNegatives()  + " negativos de "  + getTotais());
    } else {
      window.messageBus.send(event.senderId, "VOTA DIREITO!!! Já tem " + getTotais() + " votos!");
    }
  }

  // initialize the CastReceiverManager with an application status message
  window.castReceiverManager.start({statusText: "Application is starting"});
  console.log('Receiver Manager started');
};
