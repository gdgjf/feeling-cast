  // app ID
  var applicationID = '57840D57';
  var namespace = 'urn:x-cast:net.fbvictorhugo.feelingcast';
  var session = null;

  /**
  * Call initialization for Cast
  */
  if (!chrome.cast || !chrome.cast.isAvailable) {
    setTimeout(initializeCastApi, 1000);
  }

  /**
  * initialization
  */
  function initializeCastApi() {
    var sessionRequest = new chrome.cast.SessionRequest(applicationID);
    var apiConfig = new chrome.cast.ApiConfig(sessionRequest,
      sessionListener,
      receiverListener);
      chrome.cast.initialize(apiConfig, onInitSuccess, onError);
  };

  /**
  * initialization success callback
  */
  function onInitSuccess() {
    appendMessage("onInitSuccess");
  }

  /**
  * initialization error callback
  */
  function onError(message) {
    appendMessage("onError: "+JSON.stringify(message));
  }

  /**
  * generic success callback
  */
  function onSuccess(message) {
    appendMessage("onSuccess: "+message);
  }

  /**
  * callback on success for stopping app
  */
  function onStopAppSuccess() {
    appendMessage('onStopAppSuccess');
  }

  /**
 * session listener during initialization
 */
    function sessionListener(e) {
      console.log(e);
      session = e;
      session.addUpdateListener(sessionUpdateListener);
      session.addMessageListener(namespace, receiverMessage);
    }


    /**
     * listener for session updates
     */
      function sessionUpdateListener(isAlive) {
        var message = isAlive ? 'Session Updated' : 'Session Removed';
        message += ': ' + session.sessionId;
        if (!isAlive) {
          session = null;
        }
      };
  /**
  * receiver listener during initialization
  */
  function receiverListener(receivers) {
    if(receivers === chrome.cast.ReceiverAvailability.AVAILABLE) {
      console.log(receivers);
    }
  }

  /** Função para logar messagens do receiver
  * @param {string} namespace O namespace da mensagem
  * @param {string} message A mensagem em si
  */
  function receiverMessage(namespace, message) {
    appendMessage(message);
  }

  function sendMessage(message) {
    if (session!=null) {
      session.sendMessage(namespace, message, onSuccess.bind(this, "Message sent (session!=null) : " + JSON.stringify(message)), onError);
    }
    else {
      chrome.cast.requestSession(function(e) {
        session = e;
        session.addUpdateListener(sessionUpdateListener);
        session.addMessageListener(namespace, receiverMessage);
        session.sendMessage(namespace, message, onSuccess.bind(this, "Message sent: " + JSON.stringify(message)), onError);
      }, onError);
    }
  }

  function appendMessage(message) {
    console.log(message);
    var dw = document.getElementById("messages");
    dw.innerHTML =   JSON.stringify(message);
  };

function votePositive(){
    var message = document.getElementById('message').value;
    vote("positive", message);
  }

function voteNegative(){
    var message = document.getElementById('message').value;
    vote("negative", message);
  }

  function vote(choice, message) {
    var JSONObj = { "choice":choice, "message":message};
    sendMessage(JSONObj );
  }
