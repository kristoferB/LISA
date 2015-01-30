package service

import akka.actor._
import spray.routing._




/**
 * Actor for HttpServices.
 *
 * Extend this actor with Spray HttpService traits to add http services.
 * - adroute from SprayService
 * - staticRoute to static files under webapp directory
 */
/*
class ServiceActor(val sp: ActorRef) extends Actor with SprayService {

  def receive = runRoute(adRoute ~ staticRoute)


  def staticRoute: Route = {
    //path("")(getFromResource("webapp/index.html")) ~ getFromResourceDirectory("webapp")
    path("")(getFromFile("src/main/webapp/index.html")) ~ getFromDirectory("src/main/webapp")
  }

  override implicit def actorRefFactory: ActorRefFactory = context
}

object ServiceActor {
  def props(routeToMe: ActorRef) = Props(classOf[ServiceActor],routeToMe)
}*/
