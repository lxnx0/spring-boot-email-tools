/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.ozimov.springboot.mail.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.ozimov.springboot.mail.UnitTest;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.EmailAttachment;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmailAttachment;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static javax.mail.Message.RecipientType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEmailToMimeMessageTest extends EmailToMimeMessageValidators implements UnitTest {

    private static final Map<String, String> CUSTOM_HEADERS = ImmutableMap.of("key1", "value1", "key2", "value2");

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailToMimeMessage emailToMimeMessage;

    public static Email getSimpleMail(InternetAddress from, EmailAttachment... emailAttachments) throws UnsupportedEncodingException {
        final DefaultEmail.DefaultEmailBuilder builder = DefaultEmail.builder()
                .from(from)
                .replyTo(new InternetAddress("tullius.cicero@urbs.aeterna", "Marcus Tullius Cicero"))
                .to(Lists.newArrayList(new InternetAddress("roberto.trunfio@gmail.com", "titus@de-rerum.natura", "Pomponius Attĭcus")))
                .cc(Lists.newArrayList(new InternetAddress("tito55@de-rerum.natura", "Titus Lucretius Carus"),
                        new InternetAddress("info@de-rerum.natura", "Info Best Seller")))
                .bcc(Lists.newArrayList(new InternetAddress("caius-memmius@urbs.aeterna", "Caius Memmius")))
                .depositionNotificationTo(new InternetAddress("caligola@urbs.aeterna", "Gaius Iulius Caesar Augustus Germanicus"))
                .receiptTo(new InternetAddress("caligola@urbs.aeterna", "Gaius Iulius Caesar Augustus Germanicus"))
                .subject("Laelius de amicitia")
                .body("Firmamentum autem stabilitatis constantiaeque eius, quam in amicitia quaerimus, fides est.")
                .customHeaders(CUSTOM_HEADERS)
                .encoding(StandardCharsets.UTF_8.name());
        if (nonNull(emailAttachments) && emailAttachments.length > 0) {
            builder.attachments(asList(emailAttachments));
        }
        return builder.build();
    }

    public static Email getSimpleMail() throws UnsupportedEncodingException {
        return getSimpleMail(new InternetAddress("cicero@mala-tempora.currunt", "Marco Tullio Cicerone"));
    }

    public static Email getSimpleMailWithAttachments() throws UnsupportedEncodingException {
        return getSimpleMail(new InternetAddress("cicero@mala-tempora.currunt", "Marco Tullio Cicerone"),
                getCsvAttachment("test1"), getCsvAttachment("test2"));
    }

    @Test
    public void sendMailWithoutTemplate() throws MessagingException, IOException {

        // Arrange
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        final Email email = getSimpleMail();

        // Act
        final MimeMessage sentMessage = emailToMimeMessage.apply(email);

        // Assert
        validateFrom(email, sentMessage);
        validateReplyTo(email, sentMessage);
        validateTo(email, sentMessage);
        validateCc(email, sentMessage);
        validateBcc(email, sentMessage);
        validateDepositionNotification(email, sentMessage);
        validateReceipt(email, sentMessage);
        validateSubject(email, sentMessage);
        validateBody(email, sentMessage);
        validateCustomHeaders(email, sentMessage);

        verify(javaMailSender).createMimeMessage();
    }


    private static EmailAttachment getCsvAttachment(String filename) {
        final String testData = "col1,col2\n1,2\n3,4";
        final DefaultEmailAttachment attachment = DefaultEmailAttachment.builder()
                .attachmentName(filename + ".csv")
                .attachmentData(testData.getBytes(Charset.forName("UTF-8")))
                .mediaType(MediaType.TEXT_PLAIN).build();
        return attachment;
    }

}
