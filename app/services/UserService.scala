package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.User
import repository.UserRepositoryInterface

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserService @Inject()(userRepository: UserRepositoryInterface) extends UserServiceInterface {

  def insert(x: User): Future[Boolean] = {

    userRepository.insert(x).map{_ => true}
      .recover{
        case _ => false
      }
  }

  def retrieve: Future[Seq[User]] = {
    userRepository.findAll
  }

}

@ImplementedBy(classOf[UserService])
trait UserServiceInterface {
  def insert(x: User): Future[Boolean]
  def retrieve: Future[Seq[User]]
}
