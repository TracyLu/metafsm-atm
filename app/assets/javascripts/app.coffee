utils = window.angular.module('utils' , [])

utils.controller('ATMController', ($scope, $http) ->
  init = ->
    $scope.showResultFlag = false

  startWS = ->
    wsUrl = jsRoutes.controllers.AppController.indexWS().webSocketURL()
    $scope.socket = new WebSocket(wsUrl)
    $scope.socket.onmessage = (msg) ->
      $scope.$apply( ->
        console.log "received : #{msg}"
        $scope.showResultFlag = true
        $scope.cash = JSON.parse(msg.data).data
        $scope.state = JSON.parse(msg.data).state
        )

  $scope.deposit = ->
    $http.get(jsRoutes.controllers.AppController.deposit().url).success( ->)

  $scope.withdraw = ->
    $http.get(jsRoutes.controllers.AppController.withdraw().url).success( ->)

  init()
  startWS()
)

window.angular.module('app' , ['utils'])