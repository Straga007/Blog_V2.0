package Blog_V2.integration;

import Blog_V2.BlogGradleSpringWebappApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = BlogGradleSpringWebappApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ApplicationIntegrationTest {

    @Test
    public void contextLoads() {
    }
}
