package actors

import akka.actor._
import akka.testkit._

import org.specs2.mutable._
import org.specs2.time.NoTimeConversions

import scala.concurrent.duration._

import scala.collection.JavaConverters._
import play.api.test.WithApplication
import org.specs2.matcher.JsonMatchers

class UserActorSpec extends TestkitExample
                       with SpecificationLike
                       with JsonMatchers
                       with NoTimeConversions {

  /*
   * Running tests in parallel (which would ordinarily be the default) will work only if no
   * shared resources are used (e.g. top-level actors with the same name or the
   * system.eventStream).
   *
   * It's usually safer to run the tests sequentially.
   */

  sequential  // EP: just calls "args(sequential = true)",
              // I like how they have a method (or 'tag') for that though

  "UserActor" should {

    val symbol = "ABC"
    val price = 123
    val history = List[java.lang.Double](0.1, 1.0).asJava

    // EP: WithApplication "used to run 'specs' within the context of a running application"
    "send a stock when receiving a StockUpdate message" in new WithApplication {

      /*
       * EP: A fake WebSocket.Out that we can get the Actor to write to and compare what we
         see with what we want.

       * This is not a library method, the guy who wrote this wrote it.
       */
      val out = new StubOut()

      /* EP:
       * TestActorRef comes with Akka, "acts just like a normal ActorRef," though
         "you may retrieve a reference to the underlying actor to test internal logic."

       * Which we shall do shortly...
       */
      val userActorRef = TestActorRef[UserActor](Props(new UserActor(out)))
      val userActor = userActorRef.underlyingActor

      // send off the stock update...
      userActor.receive(StockUpdate(symbol, price))

      /* EP:
       * The userActor should have written the JSON to the WebSocket stub we set out.
       *
       * That's why we passed it into the UserActor constructor.
       */
      // ...and expect it to be a JSON node.
      val node = out.actual.toString
      node must /("type" -> "stockupdate")
      node must /("symbol" -> symbol)
      node must /("price" -> price)
    }

    "send the stock history when receiving a StockHistory message" in new WithApplication {
      val out = new StubOut()

      val userActorRef = TestActorRef[UserActor](Props(new UserActor(out)))
      val userActor = userActorRef.underlyingActor

      // send off the stock update...
      userActor.receive(StockHistory(symbol, history))

      // ...and expect it to be a JSON node.
      // EP: probably could have used '... mustEqual "hist" '
      out.actual.get("type").asText must beEqualTo("stockhistory")
      out.actual.get("symbol").asText must beEqualTo(symbol)
      out.actual.get("history").get(0).asDouble must beEqualTo(history.get(0))
    }
  }

}
