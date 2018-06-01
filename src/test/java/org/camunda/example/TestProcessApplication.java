package org.camunda.example;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;

@ProcessApplication(name = TestProcessApplication.NAME)
public class TestProcessApplication extends EmbeddedProcessApplication {

	public static final String NAME = "foo";
}
