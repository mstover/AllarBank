package com.lazerbank;

import java.io.File;
import java.util.Properties;

import org.coinjema.context.CoinjemaContext;

import com.lazerbank.nontest.AbstractTest;
import com.lazerbank.upload.PendingUploads;
import com.lazerbank.upload.UploadFolder;
import com.lazerbank.upload.UploadPackage;

public class TestUpload extends AbstractTest {


	public TestUpload(String arg0) {
		super(arg0);
	}

	protected String getTestDir() {
		return "upload_test_1";
	}

	public void testUpload() throws Exception {
		UploadFolder top = new UploadFolder(new File(
				"src/test/upload_test_1/assets"),new CoinjemaContext("assets"));
		Properties cats = new Properties();
		cats.setProperty("Library Name", "IA Bali On-Line Library");
		UploadFolder found = top.findMatch("ia_bali", cats, 0.1F);
		assertEquals("IA_Bali Scanbank", found.getDirectory().getName());
		cats.setProperty("Image Type","Model Photography");
		cats.setProperty("Archive","Active");
		cats.setProperty("Sub-Brand","Light Control");
		found = top.findMatch("ia_bali",cats,0.1F);
		assertEquals("IA_Bali Scanbank/Active/Model Photography/Light Control/",found.getPath());
		cats.clear();
		cats.setProperty("Library Name", "IA Bali On-Line Library");
		cats.setProperty("Image Type","Model Photography");
		cats.setProperty("Archive","Obsolete");
		found = top.findMatch("ia_bali",cats,0.1F);
		assertEquals("IA_Bali Scanbank/Obsolete/Model Photography/",found.getPath());
		assertEquals("[]",found.getExtraDirs(cats).toString());
		cats.clear();
		cats.setProperty("Library Name", "IA Bali On-Line Library");
		cats.setProperty("Image Type","Model Photography");
		cats.setProperty("Archive","Obsolete");
		cats.setProperty("Sub-Brand","Nothing There");
		found = top.findMatch("ia_bali",cats,0.1F);
		assertEquals("IA_Bali Scanbank/Obsolete/Model Photography/",found.getPath());
		assertEquals("[Nothing There]",found.getExtraDirs(cats).toString());
		cats.clear();
		cats.setProperty("Library Name", "IA Bali On-Line Library");
		cats.setProperty("Image Type","Model Photography");
		cats.setProperty("Archive","Obsolete");
		cats.setProperty("Sub-Brand","Nothing There");
		cats.setProperty("Promotion","On Sale");
		found = top.findMatch("ia_bali",cats,0.1F);
		assertEquals("IA_Bali Scanbank/Obsolete/Model Photography/",found.getPath());
		assertEquals("[Nothing There]",found.getExtraDirs(cats).toString());
		cats.clear();
		cats.setProperty("Library Name", "IA Bali On-Line Library");
		cats.setProperty("Image Type","Model Photography");
		cats.setProperty("Archive","Obsolete");
		cats.setProperty("Sub-Brand","Nothing There");
		cats.setProperty("Collection","2006 Summer");
		cats.setProperty("Promotion","On Sale");
		found = top.findMatch("ia_bali",cats,0.1F);
		assertEquals("IA_Bali Scanbank/Obsolete/Model Photography/",found.getPath());
		assertEquals("[Nothing There, 2006 Summer, On Sale]",found.getExtraDirs(cats).toString());
	}
	
	public void testUnpack() throws Exception
	{
		PendingUploads uploadPackages = new PendingUploads();
		for(UploadPackage pack : uploadPackages)
		{
			UploadFolder top = new UploadFolder(new File("src/test/upload_test_1/assets"),new CoinjemaContext("assets"));
			System.out.println("Family = "+ pack.getProductFamily() + " categories = " + pack.getMeta());
			UploadFolder match = top.findMatch(pack.getProductFamily(), pack.getMeta(), 0.1F);
			if(match != null)
			{
				try {
					match.copyFiles(pack);
				} catch(Exception e)
				{
					System.out.println("Failed to transfer upload package named " + pack.getName());
				}
			}
			else throw new Exception("Failed to find matching dir");
		}
		System.out.println("Go check");
		Thread.sleep(60000);
		File newFile = new File("src/test/upload_test_1/assets/IA_Bali Scanbank/Active/Model Photography/Double Support/AnnTaylorPencilBox.eps");
		assertTrue(newFile.exists());
		newFile.delete();
		newFile = new File("src/test/upload_test_1/assets/IA_Bali Scanbank/Active/Model Photography/Double Support/2006 Fall/AnnTaylorPencilBox.eps");
		assertTrue(newFile.exists());
		newFile.delete();
		new File("src/test/upload_test_1/assets/IA_Bali Scanbank/Active/Model Photography/Double Support/2006 Fall").delete();
		
	}

}
