import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.value


fun main() {

    data class User(val name: String)

    fun getUserGid(id: Int): IO<Either<Throwable, Int>> {
        return IO {
            println("run getUserGid $id")
            // Either.Left(RuntimeException("exception"))
             Either.Right(1)
        }
    }

    fun getUser(gid: Int): IO<Either<Throwable, User>> {
        return IO {
            println("run getUser $gid")
            Either.Right(User("user gid $gid"))
        }
    }

    val r = getUserGid(1).flatMap {
        when(it) {
            is Either.Left -> IO.just(it)
            is Either.Right -> getUser(it.b)
        }
    }

    var r2 = EitherT.monad<ForIO, Throwable>(IO.monad()).fx.monad {
        val (a) = EitherT(getUserGid(1))
        val (b) = EitherT(getUser(a))
        b
    }.value().fix()

    val result = r2.unsafeRunSync()

    result.fold({
        throw it
    }, {
        println("result: $it")
    })
}