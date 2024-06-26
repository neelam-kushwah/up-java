/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.cloudevent.factory;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.uuid.serializer.UuidSerializer;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.v1.UPayloadFormat;

class UCloudEventTest {

    private static final String DATA_CONTENT_TYPE = CloudEventFactory.PROTOBUF_CONTENT_TYPE;

    @Test
    @DisplayName("Test extracting the source from a CloudEvent.")
    public void test_extract_source_from_cloudevent() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        String source = UCloudEvent.getSource(cloudEvent);
        assertEquals("/2/1/8000", source);
    }

    @Test
    @DisplayName("Test extracting the sink from a CloudEvent when the sink exists.")
    public void test_extract_sink_from_cloudevent_when_sink_exists() {
        String sinkForTest = "//bo.cloud/petapp/1/rpc.response";

        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("sink", URI.create(sinkForTest));

        CloudEvent cloudEvent = builder.build();

        final Optional<String> sink = UCloudEvent.getSink(cloudEvent);
        assertTrue(sink.isPresent());
        assertEquals(sinkForTest, sink.get());
    }

    @Test
    @DisplayName("Test extracting the sink from a CloudEvent when the sink does not exist.")
    public void test_extract_sink_from_cloudevent_when_sink_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        final Optional<String> sink = UCloudEvent.getSink(cloudEvent);
        assertTrue(sink.isEmpty());
    }

    @Test
    @DisplayName("Test extracting the request id from a CloudEvent when the request id exists.")
    public void test_extract_requestId_from_cloudevent_when_requestId_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("reqid", "someRequestId");

        CloudEvent cloudEvent = builder.build();

        final Optional<String> requestId = UCloudEvent.getRequestId(cloudEvent);
        assertTrue(requestId.isPresent());
        assertEquals("someRequestId", requestId.get());
    }

    @Test
    @DisplayName("Test extracting the request id from a CloudEvent when the request id does not exist.")
    public void test_extract_requestId_from_cloudevent_when_requestId_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        final Optional<String> requestId = UCloudEvent.getRequestId(cloudEvent);
        assertTrue(requestId.isEmpty());
    }

    @Test
    @DisplayName("Test extracting the request id from a CloudEvent when the request id is null.")
    public void test_extract_requestId_from_cloudevent_when_requestId_value_is_null() {
        String reqid = null;
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("reqid", reqid);
        CloudEvent cloudEvent = builder.build();

        final Optional<String> requestId = UCloudEvent.getRequestId(cloudEvent);
        assertTrue(requestId.isEmpty());
    }

    @Test
    @DisplayName("Test extracting the hash from a CloudEvent when the hash exists.")
    public void test_extract_hash_from_cloudevent_when_hash_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();

        CloudEvent cloudEvent = builder.build();

        final Optional<String> hash = UCloudEvent.getHash(cloudEvent);
        assertTrue(hash.isPresent());
        assertEquals("somehash", hash.get());
    }

    @Test
    @DisplayName("Test extracting the hash from a CloudEvent when the hash does not exist.")
    public void test_extract_hash_from_cloudevent_when_hash_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withoutExtension("hash");
        CloudEvent cloudEvent = builder.build();

        final Optional<String> hash = UCloudEvent.getHash(cloudEvent);
        assertTrue(hash.isEmpty());
    }

    @Test
    @DisplayName("Test extracting the priority from a CloudEvent when the priority exists.")
    public void test_extract_priority_from_cloudevent_when_priority_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();

        CloudEvent cloudEvent = builder.build();

        final Optional<String> priority = UCloudEvent.getPriority(cloudEvent);
        assertTrue(priority.isPresent());
        Assertions.assertEquals(UCloudEvent.getCePriority(UPriority.UPRIORITY_CS1), priority.get());
    }

    @Test
    @DisplayName("Test extracting the priority from a CloudEvent when the priority does not exist.")
    public void test_extract_priority_from_cloudevent_when_priority_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withoutExtension("priority");
        CloudEvent cloudEvent = builder.build();

        final Optional<String> priority = UCloudEvent.getPriority(cloudEvent);
        assertTrue(priority.isEmpty());

        UMessage message = UCloudEvent.toMessage(cloudEvent);
        assertEquals(message.getAttributes().getPriority(), UPriority.UPRIORITY_UNSPECIFIED);
    }

    @Test
    @DisplayName("Test extracting the ttl from a CloudEvent when the ttl exists.")
    public void test_extract_ttl_from_cloudevent_when_ttl_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();

        CloudEvent cloudEvent = builder.build();

        final Optional<Integer> ttl = UCloudEvent.getTtl(cloudEvent);
        assertTrue(ttl.isPresent());
        assertEquals(3, ttl.get());
    }

    @Test
    @DisplayName("Test extracting the ttl from a CloudEvent when the ttl does not exist.")
    public void test_extract_ttl_from_cloudevent_when_ttl_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withoutExtension("ttl");
        CloudEvent cloudEvent = builder.build();

        final Optional<Integer> ttl = UCloudEvent.getTtl(cloudEvent);
        assertTrue(ttl.isEmpty());
    }

    @Test
    @DisplayName("Test extracting the token from a CloudEvent when the token exists.")
    public void test_extract_token_from_cloudevent_when_token_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();

        CloudEvent cloudEvent = builder.build();

        final Optional<String> token = UCloudEvent.getToken(cloudEvent);
        assertTrue(token.isPresent());
        assertEquals("someOAuthToken", token.get());
    }

    @Test
    @DisplayName("Test extracting the token from a CloudEvent when the token does not exist.")
    public void test_extract_token_from_cloudevent_when_token_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withoutExtension("token");
        CloudEvent cloudEvent = builder.build();

        final Optional<String> token = UCloudEvent.getToken(cloudEvent);
        assertTrue(token.isEmpty());
    }

    @Test
    @DisplayName("Test a CloudEvent has a platform communication error when the platform communication error exists.")
    public void test_cloudevent_has_platform_error_when_platform_error_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("commstatus", UCode.ABORTED_VALUE);

        CloudEvent cloudEvent = builder.build();

        assertTrue(UCloudEvent.hasCommunicationStatusProblem(cloudEvent));
        assertEquals(UCode.ABORTED, UCloudEvent.getCommunicationStatus(cloudEvent));
    }

    @Test
    @DisplayName("Test a CloudEvent has a platform communication error when the platform communication error does " +
            "not" + " exist.")
    public void test_cloudevent_has_platform_error_when_platform_error_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();

        CloudEvent cloudEvent = builder.build();

        assertFalse(UCloudEvent.hasCommunicationStatusProblem(cloudEvent));
        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent));
    }

    @Test
    @DisplayName("Test extracting the platform communication error from a CloudEvent when the platform communication "
            + "error exists but in the wrong format.")
    public void test_extract_platform_error_from_cloudevent_when_platform_error_exists_in_wrong_format() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("commstatus", "boom");

        CloudEvent cloudEvent = builder.build();

        assertFalse(UCloudEvent.hasCommunicationStatusProblem(cloudEvent));
        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent));
    }

    @Test
    @DisplayName("Test extracting the platform communication error from a CloudEvent when the platform communication "
            + "error exists.")
    public void test_extract_platform_error_from_cloudevent_when_platform_error_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("commstatus",
                UCode.INVALID_ARGUMENT_VALUE);

        CloudEvent cloudEvent = builder.build();

        assertEquals(UCode.INVALID_ARGUMENT, UCloudEvent.getCommunicationStatus(cloudEvent));
    }

    @Test
    @DisplayName("Test extracting the platform communication error from a CloudEvent when the platform communication "
            + "error does not exist.")
    public void test_extract_platform_error_from_cloudevent_when_platform_error_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent));
    }

    @Test
    @DisplayName("Test adding a platform communication error to an existing CloudEvent.")
    public void test_adding_platform_error_to_existing_cloudevent() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent));

        CloudEvent cloudEvent1 = UCloudEvent.addCommunicationStatus(cloudEvent, UCode.DEADLINE_EXCEEDED_VALUE);

        assertEquals(UCode.DEADLINE_EXCEEDED, UCloudEvent.getCommunicationStatus(cloudEvent1));
        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent));
    }

    @Test
    @DisplayName("Test adding an empty platform communication error to an existing CloudEvent, does nothing.")
    public void test_adding_empty_platform_error_to_existing_cloudevent() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent));

        CloudEvent cloudEvent1 = UCloudEvent.addCommunicationStatus(cloudEvent, null);

        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent));

        assertEquals(cloudEvent, cloudEvent1);
    }

    @Test
    @DisplayName("Test extracting creation timestamp from the CloudEvent UUID id when the id is not a UUIDV8.")
    public void test_extract_creation_timestamp_from_cloudevent_UUID_Id_when_not_a_UUIDV8_id() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();
        final Optional<Long> creationTimestamp = UCloudEvent.getCreationTimestamp(cloudEvent);
        assertTrue(creationTimestamp.isEmpty());
    }

    @Test
    @DisplayName("Test extracting creation timestamp from the CloudEvent UUIDV8 id when the id is valid.")
    public void test_extract_creation_timestamp_from_cloudevent_UUIDV8_Id_when_UUIDV8_id_is_valid() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = UuidSerializer.serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid);
        CloudEvent cloudEvent = builder.build();

        final Optional<Long> maybeCreationTimestamp = UCloudEvent.getCreationTimestamp(cloudEvent);
        assertTrue(maybeCreationTimestamp.isPresent());

        final long creationTimestamp = maybeCreationTimestamp.get();

        final OffsetDateTime now = OffsetDateTime.now();

        final Instant creationTimestampInstant = Instant.ofEpochMilli(creationTimestamp);
        final long creationTimestampInstantEpochSecond = creationTimestampInstant.getEpochSecond();
        final long nowTimeStampEpochSecond = now.toEpochSecond();

        assertEquals(creationTimestampInstantEpochSecond, nowTimeStampEpochSecond);
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when no ttl is configured.")
    public void test_cloudevent_is_not_expired_cd_when_no_ttl_configured() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withoutExtension("ttl");
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is zero.")
    public void test_cloudevent_is_not_expired_cd_when_ttl_is_zero() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 0);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is minus one.")
    public void test_cloudevent_is_not_expired_cd_when_ttl_is_minus_one() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("ttl", -1);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is 500 milliseconds " + "but no creation date.")
    public void test_cloudevent_is_not_expired_cd_when_ttl_3_mili_no_creation_date() {
        final Any protoPayload = buildProtoPayloadForTest();
        final CloudEventBuilder builder = CloudEventBuilder.v1().withId("id").withType("pub.v1")
                .withSource(URI.create("/body.accss//8000")).withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create(protoPayload.getTypeUrl())).withData(protoPayload.toByteArray())
                .withExtension("ttl", 500);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is 500 milliseconds " + "with creation date of now.")
    public void test_cloudevent_is_not_expired_cd_when_ttl_500_mili_with_creation_date_of_now() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withTime(OffsetDateTime.now())
                .withExtension("ttl", 500);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is expired using creation date when configured ttl is 500 milliseconds with "
            + "creation date of yesterday.")
    public void test_cloudevent_is_expired_cd_when_ttl_500_mili_with_creation_date_of_yesterday() {
        OffsetDateTime yesterday = OffsetDateTime.now().minus(1, ChronoUnit.DAYS);
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withTime(yesterday).withExtension("ttl", 500);
        CloudEvent cloudEvent = builder.build();
        assertTrue(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is 500 milliseconds " + "with creation date of tomorrow.")
    public void test_cloudevent_is_not_expired_cd_when_ttl_500_mili_with_creation_date_of_tomorrow() {
        OffsetDateTime tomorrow = OffsetDateTime.now().plus(1, ChronoUnit.DAYS);
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withTime(tomorrow).withExtension("ttl", 500);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when no ttl is configured.")
    public void test_cloudevent_is_not_expired_when_no_ttl_configured() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = UuidSerializer.serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withoutExtension("ttl").withId(str_uuid);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when configured ttl is zero.")
    public void test_cloudevent_is_not_expired_when_ttl_is_zero() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = UuidSerializer.serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 0).withId(str_uuid);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when configured ttl is minus one.")
    public void test_cloudevent_is_not_expired_when_ttl_is_minus_one() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = UuidSerializer.serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("ttl", -1).withId(str_uuid);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when configured ttl is large number.")
    public void test_cloudevent_is_not_expired_when_ttl_is_large_number_mili() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = UuidSerializer.serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("ttl", Integer.MAX_VALUE)
                .withId(str_uuid);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is expired when configured ttl is 1 milliseconds.")
    public void test_cloudevent_is_expired_when_ttl_1_mili() throws InterruptedException {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = UuidSerializer.serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 1).withId(str_uuid);
        CloudEvent cloudEvent = builder.build();
        Thread.sleep(800);
        assertTrue(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent isExpired when passed invalid UUID")
    public void test_cloudevent_is_expired_for_invalid_uuid() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 50000).withId("");
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent has a UUIDV8 id.")
    public void test_cloudevent_has_a_UUIDV8_id() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = UuidSerializer.serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid);
        CloudEvent cloudEvent = builder.build();
        assertTrue(UCloudEvent.isCloudEventId(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent does not have a UUIDV8 id.")
    public void test_cloudevent_does_not_have_a_UUIDV8_id() {
        final java.util.UUID uuid_java = java.util.UUID.randomUUID();
        UUID uuid = UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits())
                .setLsb(uuid_java.getLeastSignificantBits()).build();
        String str_uuid = UuidSerializer.serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 3).withId(str_uuid);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isCloudEventId(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent does not have a valid UUID id but some string")
    public void test_cloudevent_does_not_have_a_UUID_id_just_some_string() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 3);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isCloudEventId(cloudEvent));
    }

    @Test
    @DisplayName("Test extract payload from cloud event as Any protobuf object")
    public void test_extractPayload_from_cloud_event_as_any_proto_object() {
        Any payloadForCloudEvent = buildProtoPayloadForTest();
        byte[] cloudEventData = payloadForCloudEvent.toByteArray();

        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
                .withSource(URI.create("/body.access/1/8000")).withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create(payloadForCloudEvent.getTypeUrl())).withData(cloudEventData);
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final Any extracted = UCloudEvent.getPayload(cloudEvent);

        assertEquals(payloadForCloudEvent, extracted);
    }

    @Test
    @DisplayName("Test extract payload from cloud event when payload is not an Any protobuf object")
    public void test_extractPayload_from_cloud_event_when_payload_is_not_an_any_proto_object()
            throws InvalidProtocolBufferException {
        io.cloudevents.v1.proto.CloudEvent payloadForCloudEvent = buildProtoPayloadForTest1();
        byte[] cloudEventData = payloadForCloudEvent.toByteArray();

        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
                .withSource(URI.create("/body.access/1/8000")).withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create("type.googleapis.com/io.cloudevents.v1.CloudEvent"))
                .withData(cloudEventData);
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final CloudEventData data = cloudEvent.getData();
        final Any dataAsAny = Any.parseFrom(data.toBytes());

        final Any extracted = UCloudEvent.getPayload(cloudEvent);

        assertEquals(dataAsAny, extracted);
    }

    @Test
    @DisplayName("Test extract payload from cloud event when payload is a bad protobuf object")
    public void test_extractPayload_from_cloud_event_when_payload_is_bad_proto_object() {
        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
                .withSource(URI.create("/body.access/1/8000")).withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create("type.googleapis.com/io.cloudevents.v1.CloudEvent"))
                .withData("<html><head></head><body><p>Hello</p></body></html>".getBytes());
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final Any extracted = UCloudEvent.getPayload(cloudEvent);

        assertEquals(Any.getDefaultInstance(), extracted);
    }

    @Test
    @DisplayName("Test extract payload from cloud event as Any protobuf object when there is no data schema")
    public void test_extractPayload_from_cloud_event_as_any_proto_object_when_no_schema() {
        Any payloadForCloudEvent = buildProtoPayloadForTest();
        byte[] cloudEventData = payloadForCloudEvent.toByteArray();

        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
                .withSource(URI.create("/body.access/1/8000")).withDataContentType(DATA_CONTENT_TYPE)
                .withData(cloudEventData);
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final Any extracted = UCloudEvent.getPayload(cloudEvent);

        assertEquals(payloadForCloudEvent, extracted);
    }

    @Test
    @DisplayName("Test extract payload from cloud event as Any protobuf object when there is no data")
    public void test_extractPayload_from_cloud_event_as_any_proto_object_when_no_data() {
        Any payloadForCloudEvent = buildProtoPayloadForTest();

        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
                .withSource(URI.create("/body.access/1/8000")).withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create(payloadForCloudEvent.getTypeUrl()));
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final Any extracted = UCloudEvent.getPayload(cloudEvent);

        assertEquals(Any.getDefaultInstance(), extracted);
    }


    @Test
    @DisplayName("Test pretty printing a cloud event with a sink")
    public void test_pretty_printing_a_cloudevent_with_a_sink() {

        String sinkForTest = "//bo.cloud/petapp/1/rpc.response";

        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("sink", URI.create(sinkForTest));
        CloudEvent cloudEvent = builder.build();

        final String prettyPrint = UCloudEvent.toString(cloudEvent);
        final String expected = "CloudEvent{id='testme', source='/2/1/8000', " + "sink='//bo"
                + ".cloud/petapp/1/rpc.response', type='pub.v1'}";

        assertEquals(expected, prettyPrint);
    }

    @Test
    @DisplayName("Test pretty printing a cloud event that is null")
    public void test_pretty_printing_a_cloudevent_that_is_null() {

        final String prettyPrint = UCloudEvent.toString(null);
        final String expected = "null";

        assertEquals(expected, prettyPrint);
    }

    @Test
    @DisplayName("Test pretty printing a cloud event without a sink")
    public void test_pretty_printing_a_cloudevent_without_a_sink() {
        CloudEvent cloudEvent = buildBaseCloudEventBuilderForTest().build();

        final String prettyPrint = UCloudEvent.toString(cloudEvent);
        final String expected = "CloudEvent{id='testme', source='/2/1/8000', type='pub.v1'}";

        assertEquals(expected, prettyPrint);
    }

    private CloudEventBuilder buildBaseCloudEventBuilderForTest() {
        // source
        UUri Uri = UUri.newBuilder().setUeId(2)
                .setUeVersionMajor(1)
                .setResourceId(0x8000)
                .build();

        String source = UriSerializer.serialize(Uri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes =
                new UCloudEventAttributes.UCloudEventAttributesBuilder().withHash(
                        "somehash").withPriority(UPriority.UPRIORITY_CS1).withTtl(3).withToken(
                                "someOAuthToken")
                .build();

        // build the cloud event
        final CloudEventBuilder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                 protoPayload, uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));

        return cloudEventBuilder;
    }

    private Any buildProtoPayloadForTest() {
        return Any.pack(buildProtoPayloadForTest1());
    }

    private io.cloudevents.v1.proto.CloudEvent buildProtoPayloadForTest1() {
        return io.cloudevents.v1.proto.CloudEvent.newBuilder().setSpecVersion("1.0").setId("hello")
                .setSource("//VCU.MY_CAR_VIN/2/1/8000").setType("example.demo")
                .setProtoData(Any.newBuilder().build()).build();
    }
    @Test
    @DisplayName("Test the type for a publish message type")
    public void test_type_for_publish() {
        String uCloudEventType = UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH);
        assertEquals("pub.v1", uCloudEventType);
    }


    @Test
    @DisplayName("Test the type for a request RPC message type")
    public void test_type_for_request() {
        String uCloudEventType = UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST);
        assertEquals("req.v1", uCloudEventType);
    }

    @Test
    @DisplayName("Test the type for a response RPC message type")
    public void test_type_for_response() {
        String uCloudEventType = UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE);
        assertEquals("res.v1", uCloudEventType);
    }

    @Test
    @DisplayName("Test the type for a unspecified message type")
    public void test_parse_publish_event_type_from_string() {
        String uCloudEventType = UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_UNSPECIFIED);
        assertTrue(uCloudEventType.isBlank());
    }

    @Test
    public void test_to_message_with_valid_event() {
        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withPriority(UPriority.UPRIORITY_CS2)
                .withTtl(3)
                .build();
        //cloudevent
        final CloudEvent cloudEvent = CloudEventFactory.publish(buildSourceForTest(), buildProtoPayloadForTest(),
                uCloudEventAttributes);
        UMessage uMessage = UCloudEvent.toMessage(cloudEvent);

        assertNotNull(uMessage);


    }
    @Test
    public void test_from_message_with_valid_message() {
        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withPriority(UPriority.UPRIORITY_CS2)
                .withTtl(3)
                .withTraceparent("someParent")
                .build();
        //cloudevent
        final CloudEvent cloudEvent = CloudEventFactory.publish(buildSourceForTest(), buildProtoPayloadForTest(),
                uCloudEventAttributes);
        UMessage uMessage = UCloudEvent.toMessage(cloudEvent);

        assertNotNull(uMessage);
        CloudEvent cloudEvent1 = UCloudEvent.fromMessage(uMessage);

        assertNotNull(cloudEvent1);
        assertEquals(cloudEvent,cloudEvent1);
    }

    @Test
    public void test_to_from_message_from_request_cloudevent() {
        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withPriority(UPriority.UPRIORITY_CS2)
                .withToken("someOAuthToken")
                .withTtl(3)
                .build();
        //cloudevent
        final CloudEvent cloudEvent = CloudEventFactory.request(buildSourceForTest(),
        "//bo.cloud/2/1/0", buildProtoPayloadForTest(), uCloudEventAttributes);


        UMessage result = UCloudEvent.toMessage(cloudEvent);
        assertNotNull(result);
        assertTrue(UCloudEvent.getTtl(cloudEvent).isPresent());
        assertEquals(UCloudEvent.getTtl(cloudEvent).get(), result.getAttributes().getTtl());
        assertTrue(UCloudEvent.getToken(cloudEvent).isPresent());
        assertEquals(UCloudEvent.getToken(cloudEvent).get(), result.getAttributes().getToken());
        assertTrue(UCloudEvent.getSink(cloudEvent).isPresent());
        assertEquals(UCloudEvent.getSink(cloudEvent).get(),
                UriSerializer.serialize(result.getAttributes().getSink()));
        assertEquals(UCloudEvent.getPayload(cloudEvent).toByteString(),result.getPayload());
        assertEquals(UCloudEvent.getSource(cloudEvent),UriSerializer.serialize(result.getAttributes().getSource()));
        assertTrue(UCloudEvent.getPriority(cloudEvent).isPresent());
        assertEquals(UCloudEvent.getPriority(cloudEvent).get(), UCloudEvent.getCePriority(result.getAttributes().getPriority()));

        final CloudEvent cloudEvent1 = UCloudEvent.fromMessage(result);
        assertNotNull(cloudEvent1);
        assertEquals(cloudEvent,cloudEvent1);
    }

    @Test
    public void test_to_from_message_from_request_cloudevent_without_attributes() {
        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .build();
        //cloudevent
        final CloudEvent cloudEvent = CloudEventFactory.request(buildSourceForTest(),"//bo.cloud/2/1/0", buildProtoPayloadForTest(),
                uCloudEventAttributes);

        UMessage result = UCloudEvent.toMessage(cloudEvent);
        assertNotNull(result);
        assertEquals(result.getAttributes().getTtl(), 0);
        assertTrue(UCloudEvent.getSink(cloudEvent).isPresent());
        assertEquals(UCloudEvent.getSink(cloudEvent).get(),
                UriSerializer.serialize(result.getAttributes().getSink()));
        assertEquals(UCloudEvent.getPayload(cloudEvent).toByteString(),result.getPayload());
        assertEquals(UCloudEvent.getSource(cloudEvent),UriSerializer.serialize(result.getAttributes().getSource()));
        assertEquals(result.getAttributes().getPriority(),UPriority.UPRIORITY_UNSPECIFIED);

        assertEquals(result.getPayload(), buildProtoPayloadForTest().toByteString());

        final CloudEvent cloudEvent1 = UCloudEvent.fromMessage(result);
        assertEquals(cloudEvent,cloudEvent1);

    }

    @Test
    public void test_to_from_message_from_response_cloudevent() {
        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withPriority(UPriority.UPRIORITY_CS2)
                .withTtl(3)
                .build();
        //cloudevent
        final CloudEvent cloudEvent = CloudEventFactory.response(buildSourceForTest(),"//bo.cloud/2/1/0",
            UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create()),
                buildProtoPayloadForTest(), uCloudEventAttributes);

        UMessage result = UCloudEvent.toMessage(cloudEvent);
        assertNotNull(result);
        assertTrue(UCloudEvent.getRequestId(cloudEvent).isPresent());
        assertEquals(UCloudEvent.getRequestId(cloudEvent).get(),
                UuidSerializer.serialize(result.getAttributes().getReqid()));
        assertTrue(UCloudEvent.getTtl(cloudEvent).isPresent());
        assertEquals(UCloudEvent.getTtl(cloudEvent).get(), result.getAttributes().getTtl());
        assertTrue(UCloudEvent.getSink(cloudEvent).isPresent());
        assertEquals(UCloudEvent.getSink(cloudEvent).get(),
                UriSerializer.serialize(result.getAttributes().getSink()));
        assertEquals(UCloudEvent.getPayload(cloudEvent).toByteString(),result.getPayload());
        assertEquals(UCloudEvent.getSource(cloudEvent),UriSerializer.serialize(result.getAttributes().getSource()));
        assertTrue(UCloudEvent.getPriority(cloudEvent).isPresent());
        assertEquals(UCloudEvent.getPriority(cloudEvent).get(), UCloudEvent.getCePriority(result.getAttributes().getPriority()));

        final CloudEvent cloudEvent1 = UCloudEvent.fromMessage(result);
        assertEquals(cloudEvent,cloudEvent1);
    }
    @Test
    public void test_umessage_has_platform_error_when_platform_error_exists() {
        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withPriority(UPriority.UPRIORITY_CS2)
                .withTtl(3)
                .build();

        Any protoPayload= buildProtoPayloadForTest();
        final CloudEventBuilder cloudEventBuilder =
                CloudEventFactory.buildBaseCloudEvent(UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create()), buildSourceForTest(),
                 protoPayload, uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
                .withExtension("commstatus", UCode.ABORTED_VALUE).withExtension("plevel",2);

        CloudEvent cloudEvent = cloudEventBuilder.build();
        UMessage result = UCloudEvent.toMessage(cloudEvent);
        assertNotNull(result);
        assertEquals(UCode.ABORTED, UCloudEvent.getCommunicationStatus(cloudEvent));
        assertEquals(2, result.getAttributes().getPermissionLevel());

        CloudEvent cloudEvent1 = UCloudEvent.fromMessage(result);
        assertEquals(cloudEvent,cloudEvent1);

    }

    @Test
    @DisplayName("Test to/from UMesage when there is no Payload")
    public void test_to_from_message_when_no_payload() {
        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withPriority(UPriority.UPRIORITY_CS2)
                .withTtl(3)
                .build();
        //cloudevent
        final CloudEvent cloudEvent = CloudEventFactory.publish(buildSourceForTest(), null,
                uCloudEventAttributes);

        UMessage result = UCloudEvent.toMessage(cloudEvent);
        assertNotNull(result);
        assertEquals(result.getAttributes().getTtl(), 3);
        assertEquals(result.getAttributes().getPriority(), UPriority.UPRIORITY_CS2);
        assertFalse(result.hasPayload());
        assertEquals(result.getAttributes().getPayloadFormat(), UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED);

        final CloudEvent cloudEvent1 = UCloudEvent.fromMessage(result);
        assertEquals(cloudEvent,cloudEvent1);
    }


    @Test
    public void testToMessageWithNullEvent() {
        assertThrows(NullPointerException.class, () -> UCloudEvent.toMessage(null));
    }

    @Test
    public void test_to_from_message_from_cloudevent_with_all_payload_formats() {
        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withPriority(UPriority.UPRIORITY_CS2)
                .withTtl(3)
                .build();

        Any protoPayload= buildProtoPayloadForTest();
        final CloudEventBuilder cloudEventBuilder =
                CloudEventFactory.buildBaseCloudEvent(UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create()), buildSourceForTest(),
                         protoPayload, uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));

        CloudEvent cloudEvent = cloudEventBuilder.build();

        UMessage result = UCloudEvent.toMessage(cloudEvent);
        assertNotNull(result);
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY,result.getAttributes().getPayloadFormat());

        final CloudEvent cloudEvent1 = UCloudEvent.fromMessage(result);
        assertEquals(cloudEvent,cloudEvent1);
        assertNull(cloudEvent1.getDataContentType());

        final CloudEvent cloudEvent2 = cloudEventBuilder.withDataContentType("").build();
        result = UCloudEvent.toMessage(cloudEvent2);
        assertNotNull(result);
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY,result.getAttributes().getPayloadFormat());
        final CloudEvent cloudEvent3 = UCloudEvent.fromMessage(result);
        assertNull(cloudEvent3.getDataContentType());

        final CloudEvent cloudEvent4 = cloudEventBuilder.withDataContentType("application/json").build();
        result = UCloudEvent.toMessage(cloudEvent4);
        assertNotNull(result);
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_JSON,result.getAttributes().getPayloadFormat());
        final CloudEvent cloudEvent5 = UCloudEvent.fromMessage(result);
        assertEquals(cloudEvent4,cloudEvent5);
        assertEquals("application/json",cloudEvent5.getDataContentType());

        final CloudEvent cloudEvent6 = cloudEventBuilder.withDataContentType("application/octet-stream").build();
        result = UCloudEvent.toMessage(cloudEvent6);
        assertNotNull(result);
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_RAW,result.getAttributes().getPayloadFormat());
        final CloudEvent cloudEvent7 = UCloudEvent.fromMessage(result);
        assertEquals(cloudEvent6,cloudEvent7);
        assertEquals("application/octet-stream",cloudEvent7.getDataContentType());

        final CloudEvent cloudEvent8 = cloudEventBuilder.withDataContentType("text/plain").build();
        result = UCloudEvent.toMessage(cloudEvent8);
        assertNotNull(result);
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_TEXT,result.getAttributes().getPayloadFormat());
        final CloudEvent cloudEvent9 = UCloudEvent.fromMessage(result);
        assertEquals(cloudEvent8,cloudEvent9);
        assertEquals("text/plain",cloudEvent9.getDataContentType());

        final CloudEvent cloudEvent10 = cloudEventBuilder.withDataContentType("application/x-someip").build();
        result = UCloudEvent.toMessage(cloudEvent10);
        assertNotNull(result);
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_SOMEIP,result.getAttributes().getPayloadFormat());
        final CloudEvent cloudEvent11 = UCloudEvent.fromMessage(result);
        assertEquals(cloudEvent10,cloudEvent11);
        assertEquals("application/x-someip",cloudEvent11.getDataContentType());

        final CloudEvent cloudEvent12 = cloudEventBuilder.withDataContentType("application/x-someip_tlv").build();
        result = UCloudEvent.toMessage(cloudEvent12);
        assertNotNull(result);
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_SOMEIP_TLV,result.getAttributes().getPayloadFormat());
        final CloudEvent cloudEvent13 = UCloudEvent.fromMessage(result);
        assertEquals(cloudEvent12,cloudEvent13);
        assertEquals("application/x-someip_tlv",cloudEvent13.getDataContentType());
    }
    

    @Test
    public void test_to_from_message_from_UCP_cloudevent(){
        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withTtl(3)
                .build();

        Any protoPayload= buildProtoPayloadForTest();
        final CloudEventBuilder cloudEventBuilder =
                CloudEventFactory.buildBaseCloudEvent(UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create()), buildSourceForTest(),
                         protoPayload, uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));
        cloudEventBuilder.withExtension("priority","CS4");

        CloudEvent cloudEvent = cloudEventBuilder.build();

        UMessage result = UCloudEvent.toMessage(cloudEvent);
        assertNotNull(result);
        assertEquals(UCloudEvent.getCePriority(UPriority.UPRIORITY_CS4),UCloudEvent.getCePriority(result.getAttributes().getPriority()));
        CloudEvent cloudEvent1 = UCloudEvent.fromMessage(result);
        assertTrue(UCloudEvent.getPriority(cloudEvent1).isPresent());
        assertEquals(UCloudEvent.getCePriority(UPriority.UPRIORITY_CS4),UCloudEvent.getPriority(cloudEvent1).get());

    }

    @Test
    @DisplayName("Test fromMessage when payload is empty")
    public void test_from_message_when_payload_is_empty() {
        final UMessage message = UMessageBuilder.publish(UUri.newBuilder().setUeId(2).setResourceId(0x8000).build()).build();
        final CloudEvent ce = UCloudEvent.fromMessage(message);
        assertNotNull(message);
        assertNotNull(ce);
        assertNull(ce.getData());
    }

    
    private String buildSourceForTest(){
        UUri Uri = UUri.newBuilder().setUeId(2)
                .setResourceId(0x8000)
                .build();

        return UriSerializer.serialize(Uri);
    }

    @Test
    @DisplayName("Test fromMessage passing null")
    public void test_fromMessage_with_null_message() {
        try {
            UCloudEvent.fromMessage(null);
        } catch (NullPointerException e) {
            assertEquals("message cannot be null.", e.getMessage());
        }
    }

}