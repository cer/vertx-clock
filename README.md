Vertex clock
============

This application uses Vertx + SockJS to show a web page that displays a constantly incrementing counter.
The Timer runs within Vertx and pushes events to the browser using SocketJS.

You can build and deploy on Cloud Foundry using the following command:

	gradle assembleApp cf-push -PcfUser=<YourUserId> -PcfPasswd=<YourPassword>

Please note
* Vertx requires Java 7
