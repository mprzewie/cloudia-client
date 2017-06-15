import client._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    val servletName = "node"
    context.mount(new CloudiaClientServlet(servletName), s"/$servletName/*")
  }
}
