package com.hw.springboot.clazz;

import com.hw.springboot.mock.RandomService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class AsyncStageServiceManagerTest {

    @Test
    public void test_different_delay() throws InterruptedException {
        RandomService a = new RandomService("A", 1000);
        RandomService b = new RandomService("B", 1000);
        RandomService c = new RandomService("C", 1000);
        RandomService d = new RandomService("D", 1000);
        RandomService e = new RandomService("E", 1000);
        RandomService f = new RandomService("F", 1000);
        AbstractService g = new RandomService("G", 2000);
        AbstractService h = new RandomService("H", 2000);
        AbstractService i = new RandomService("I", 2000);
        f.blockedBy(d);
        f.blockedBy(e);
        d.blockedBy(c);
        d.blockedBy(i);
        e.blockedBy(c);
        c.blockedBy(b);
        c.blockedBy(g);
        b.blockedBy(a);
        b.blockedBy(h);
        List<AbstractService> abstractServices = Arrays.asList(a, b, c, d, e, f, g, h, i);
        abstractServices.stream().forEach(e1 -> System.out.println(e1.getName() + "::" + e1.priority));
        AsyncServiceExecutor asyncServiceExecutor = new AsyncServiceExecutor();
        asyncServiceExecutor.addAll(a, b, c, d, e, f, g, h, i);
        long l = System.currentTimeMillis();
        asyncServiceExecutor.executeReadyServiceImproved();
        log.info("execution time :: {}", System.currentTimeMillis() - l);
        Assert.isTrue(System.currentTimeMillis() - l < 7000L,"error - execution time is longer then expected");
    }

    @Test
    public void test_simple_case() throws InterruptedException {
        RandomService policy_generation = new RandomService("Policy Generation", 1000);
        RandomService pdf_generation = new RandomService("PDF Generation", 1000);
        RandomService save_pdf = new RandomService("Save PDF", 1000);
        RandomService submit = new RandomService("Submit", 1000);
        RandomService translation = new RandomService("Translation", 1000);
        RandomService fetch_document = new RandomService("Fetch Document", 1000);
        submit.blockedBy(fetch_document);
        submit.blockedBy(save_pdf);
        save_pdf.blockedBy(pdf_generation);
        pdf_generation.blockedBy(policy_generation);
        pdf_generation.blockedBy(translation);
        List<AbstractService> abstractServices = Arrays.asList(policy_generation, pdf_generation, save_pdf, submit, translation, fetch_document);
        abstractServices.stream().forEach(e1 -> System.out.println(e1.getName() + "::" + e1.priority));
        AsyncServiceExecutor asyncServiceExecutor = new AsyncServiceExecutor();
        asyncServiceExecutor.addAll(policy_generation, pdf_generation, save_pdf, submit, translation, fetch_document);
        long l = System.currentTimeMillis();
        asyncServiceExecutor.executeReadyServiceImproved();
        log.info("execution time :: {}", System.currentTimeMillis() - l);
        Assert.isTrue(System.currentTimeMillis() - l < 5000L,"error - execution time is longer then expected");
    }

    @Test
    public void test_a_lot_independent_service() throws InterruptedException {
        RandomService policy_generation = new RandomService("Policy Generation", 1000);
        RandomService pdf_generation = new RandomService("PDF Generation", 1000);
        RandomService save_pdf = new RandomService("Save PDF", 1000);
        RandomService submit = new RandomService("Submit", 1000);
        RandomService translation = new RandomService("Translation", 1000);
        RandomService fetch_document0 = new RandomService("Fetch Document - 0", 1000);
        RandomService fetch_document1 = new RandomService("Fetch Document - 1", 1000);
        RandomService fetch_document2 = new RandomService("Fetch Document - 2", 1000);
        RandomService fetch_document3 = new RandomService("Fetch Document - 3", 1000);
        RandomService fetch_document4 = new RandomService("Fetch Document - 4", 1000);
        submit.blockedBy(fetch_document0);
        submit.blockedBy(fetch_document1);
        submit.blockedBy(fetch_document2);
        submit.blockedBy(fetch_document3);
        submit.blockedBy(save_pdf);
        save_pdf.blockedBy(pdf_generation);
        pdf_generation.blockedBy(policy_generation);
        pdf_generation.blockedBy(translation);
        List<AbstractService> abstractServices = Arrays.asList(policy_generation, pdf_generation, save_pdf, submit, translation, fetch_document0, fetch_document1, fetch_document2, fetch_document3, fetch_document4);
        abstractServices.stream().forEach(e1 -> System.out.println(e1.getName() + "::" + e1.priority));
        AsyncServiceExecutor asyncServiceExecutor = new AsyncServiceExecutor();
        asyncServiceExecutor.addAll(policy_generation, pdf_generation, save_pdf, submit, translation, fetch_document0, fetch_document1, fetch_document2, fetch_document3, fetch_document4);
        long l = System.currentTimeMillis();
        asyncServiceExecutor.executeReadyServiceImproved();
        log.info("execution time :: {}", System.currentTimeMillis() - l);
        Assert.isTrue(System.currentTimeMillis() - l < 5000L,"error - execution time is longer then expected");
    }

}
