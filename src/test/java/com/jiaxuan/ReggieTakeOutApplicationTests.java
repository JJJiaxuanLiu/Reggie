package com.jiaxuan;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReggieTakeOutApplicationTests {

    @Test
    void contextLoads() {
    }


    @Test
    public void testSplit(){
        String str = "12214343,45345";
        String[] strings = str.split(",");
        System.out.println(strings[0]);
        System.out.println(strings[1]);
    }

}
