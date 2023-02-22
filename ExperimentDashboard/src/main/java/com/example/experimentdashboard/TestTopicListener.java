
package com.example.experimentdashboard;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;


/**
 * This class extends {@link AWSIotTopic} to receive messages from a subscribed
 * topic.
 */
public class TestTopicListener extends AWSIotTopic {

	DashboardController controller;
    public TestTopicListener(String topic, AWSIotQos qos, DashboardController controller) {
        super(topic, qos);
        this.controller = controller;
    }

    @Override
    public void onMessage(AWSIotMessage message) {
        System.out.println(System.currentTimeMillis() + ": <<< " + message.getStringPayload());
        controller.updateOutputArea(message.getStringPayload());
    }

}