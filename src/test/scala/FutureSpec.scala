import org.scalatest.FlatSpec
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Success

class FutureSpec extends FlatSpec{

  behavior of "A Future"

  it should "recover" in {

    def f(x:Int):Int = {
      Thread.sleep(2)
      if(x%2==0) x
      else throw new Exception("f dont process evens")
    }

    val failedFuture = Future {
      f(3)
    }.recover{
      case e:Exception => 0
    }

    val res = Await.result(failedFuture, 3 seconds)

    assert(res == 0)

  }

  it should "recover with another Future" in {

    def f(x:Int):Int = {
      Thread.sleep(2)
      if(x%2==0) x
      else throw new Exception("f dont process evens")
    }

    def recoveryFunction: Future[Int] = Future {
      0
    }

    val failedFuture: Future[Any] = Future {
      f(3)
    }.recoverWith{
      case e:Exception => recoveryFunction
    }

    val res = Await.result(failedFuture, 3 seconds)

    assert(res == 0)

  }

  it should "compute its value in the future with flatmap" in {

    val stringResultFuture: Future[String] = Future {
      Thread.sleep(2)
      "RESULT"
    }.recoverWith{
      case e:Exception => Future("SALVAVIDAS!!")
    }

    val flatMappedFuture = stringResultFuture.flatMap(x => Future(x+" IN THE FUTURE"))

    val res0 = Await.result(flatMappedFuture, 3 seconds)
    val res1 = Await.result(stringResultFuture, 3 seconds)

    // You have to take into account that a Future is immutable!
    assert(res1 == "RESULT")
    assert(res0 == "RESULT IN THE FUTURE")
  }

  it should "compute its value in the future with for-comp" in {

    val stringResultFuture: Future[String] = Future {
      Thread.sleep(2)
      "RESULT"
    }.recoverWith{
      case e:Exception => Future("SALVAVIDAS!!")
    }

    val newFuture = for{
      x <- stringResultFuture
    } yield(x + " IN THE FUTURE")

    val res0 = Await.result(newFuture, 3 seconds)

    assert(res0 == "RESULT IN THE FUTURE")
  }

}
