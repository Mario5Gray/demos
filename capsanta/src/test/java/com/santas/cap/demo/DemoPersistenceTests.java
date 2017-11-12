package com.santas.cap.demo;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

@DataJpaTest
@SpringBootTest
@RunWith(SpringRunner.class)
public class DemoPersistenceTests {

    @Autowired
    TestEntityManager em;

    @Autowired
    SignageRepository signageRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    public void RepoShouldSaveFindUsers() {
        User test = new User(null, "mariorocks", "mario gray", "8885551234");
        userRepository.save(test);

        User foundUser = userRepository.getUsersByAccountId("mariorocks");
        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getId()).isGreaterThan(0);
        Assertions.assertThat(foundUser.getAccountId()).isEqualTo("mariorocks");
        Assertions.assertThat(foundUser.getName()).isEqualTo("mario gray");
        Assertions.assertThat(foundUser.getTele()).isEqualTo("8885551234");
    }


    @Test
    public void RepoShouldSaveFind() {
        Signage test = new Signage(null, "DOCHASH", "SIGNERID", 123456L);
        signageRepository.save(test);

        Collection<Signage> docs = signageRepository.getByDocId("DOCHASH");

        Assertions.assertThat(docs).isNotEmpty();
        Assertions.assertThat(docs.iterator().next()).isNotNull();
        Assertions.assertThat(docs.iterator().next().getId()).isNotNull();
        Assertions.assertThat(docs.iterator().next().getSignerId()).isNotNull();
        Assertions.assertThat(docs.iterator().next().getTimeInMillis()).isEqualTo(123456L);
        Assertions.assertThat(docs.iterator().next().getDocId()).isEqualTo("DOCHASH");
    }

    @Test
    public void testShouldWriteFind() {
        Signage test =
                em.persistFlushFind(
                        new Signage(null,
                                "DOCHASH",
                                "TESTHASH",
                                123456L
                        )
                );

        Assertions.assertThat(test).isNotNull();
        Assertions.assertThat(test.getId()).isGreaterThan(0L);
        Assertions.assertThat(test.getSignerId()).isEqualTo("TESTHASH");
        Assertions.assertThat(test.getTimeInMillis()).isEqualTo(123456L);
        Assertions.assertThat(test.getDocId()).isEqualTo("DOCHASH");
    }
}
