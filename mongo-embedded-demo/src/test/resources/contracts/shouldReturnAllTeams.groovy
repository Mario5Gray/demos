import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.HttpMethods

Contract.make {
    description "A Contract for all Users"
    request {
        method HttpMethods.HttpMethod.GET
        url "/all"
    }
    response {
        body(
        """
            [ 
            { "id": "1234", "name" : "Mario" },
            { "id": "2345", "name" : "Lilo" }
            ] 
        """
        )
        status(200)
        headers {
                    contentType(applicationJsonUtf8())
        }
    }
}