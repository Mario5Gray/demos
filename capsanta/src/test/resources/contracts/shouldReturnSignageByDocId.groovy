import org.springframework.cloud.contract.spec.Contract
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

Contract.make {
    description "Cap Santas Document Rest Service"

    request {
        method GET()
        url "/byDocId/doc1"
    }

    response {
        status 200
        headers {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
        }
        body([  [id: 1L, docId: "DOC1", signerId: "PERSON1", timeInMillis: 123456L],
                [id: 2L, docId: "DOC1", signerId: "PERSON2", timeInMillis: 123456L]
            ])
    }
}