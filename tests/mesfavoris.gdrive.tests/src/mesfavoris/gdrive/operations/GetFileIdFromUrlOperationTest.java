package mesfavoris.gdrive.operations;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class GetFileIdFromUrlOperationTest {
	private GetFileIdFromUrlOperation operation;

	@Before
	public void setUp() {
		operation = new GetFileIdFromUrlOperation();
	}

	@Test
	public void testGetFileId() {
		assertEquals("0B7a_ei8brT1TMy1CQ0o5NmZQNEE",
				operation
						.getFileId(
								"https://docs.google.com/a/mycompany.com/document/d/0B7a_ei8brT1TMy1CQ0o5NmZQNEE/edit?usp=sharing")
						.get());

		assertEquals("0B7a_ei8brT1TMy1CQ0o5NmZQNEE", operation
				.getFileId("https://drive.google.com/file/d/0B7a_ei8brT1TMy1CQ0o5NmZQNEE/view?usp=sharing").get());
		assertEquals("0B7a_ei8brT1TMy1CQ0o5NmZQNEE",
				operation.getFileId("https://drive.google.com/open?id=0B7a_ei8brT1TMy1CQ0o5NmZQNEE").get());
		assertEquals("0B7a_ei8brT1TQTUyc1JKYW9nTHM", operation.getFileId("https://drive.google.com/drive/u/3/folders/0B7a_ei8brT1TQTUyc1JKYW9nTHM").get());
	}

}
