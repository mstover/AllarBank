package com.lazerbank;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import strategiclibrary.util.Converter;

import com.lazerbank.nontest.AbstractTest;
import com.lazerinc.ecommerce.CommerceProduct;
import com.lazerinc.ecommerce.DatabaseUtilities;

public class Basics extends AbstractTest {

	public Basics(String arg0) {
		super(arg0);
	}
	
	protected String getTestDir()
	{
		return "test_1";
	}

	public void testInfoGather() throws Exception
	{
		ImageMagick im = new ImageMagick();
		Map<String,Object> image = im.getMagickProperties(new File("src/test/test_1/assets/ba_mod_8380_wh_02.jpg"));
		System.out.println("IMage properties = " + image);
		assertEquals("RGB",image.get("Color Type"));
		assertEquals(72D,image.get("Resolution"));
		assertEquals(108409.856D,image.get("File Size"));
		assertEquals(1046D,image.get("Width"));
		assertEquals(1280D,image.get("Height"));
		assertEquals("JPEG",image.get("File Type"));
		
		image = im.getMagickProperties(new File("src/test/test_1/assets/ba_logo_bf_stacked.eps"));
		System.out.println("IMage properties = " + image);
		assertEquals("CMYK",image.get("Color Type"));
		assertEquals(72,Converter.getInt(image.get("Resolution")));
		assertEquals(498965.504D,image.get("File Size"));
		assertEquals(447D,image.get("Width"));
		assertEquals(279D,image.get("Height"));
		assertEquals("EPS",image.get("File Type"));

		image = im.getMagickProperties(new File("src/test/test_1/assets/wb_icon_extravagance.eps"));
		System.out.println("IMage properties = " + image);
		assertEquals("CMYK",image.get("Color Type"));
		assertEquals(72,Converter.getInt(image.get("Resolution")));
		assertEquals(201306.112D,image.get("File Size"));
		assertEquals(202D,image.get("Width"));
		assertEquals(249D,image.get("Height"));
		assertEquals("EPS",image.get("File Type"));

		/*image = im.getMagickProperties(new File("src/test/test_1/assets/wb_mod_7571POS_bk.tif"));
		System.out.println("IMage properties = " + image);
		assertEquals("CMYK",image.get("Color Type"));
		assertEquals(300D,image.get("Resolution"));
		assertEquals(218025164.8D,image.get("File Size"));
		assertEquals(13572D,image.get("Width"));
		assertEquals(9271D,image.get("Height"));
		assertEquals("TIFF",image.get("File Type"));*/

		image = im.getMagickProperties(new File("src/test/test_1/assets/VSM300xx1FemaleNurse.tif"));
		System.out.println("IMage properties = " + image);
		assertEquals("RGB",image.get("Color Type"));
		assertEquals(300D,image.get("Resolution"));
		assertEquals(31542948.6592D,image.get("File Size"));
		assertEquals(4016D,image.get("Width"));
		assertEquals(2616D,image.get("Height"));
		assertEquals("TIFF",image.get("File Type"));
	}

	public void testFamilyRefresh() throws Exception
	{
		DatabaseUtilities dbutil = new DatabaseUtilities();
		new FamilyRefresher(dbutil.getProductFamily("ia_bali")).refresh();
	}
	
	public void testPropertyFileParse() throws Exception
	{
		DigitalAsset da = new DigitalAsset("/home/mikes/Workspace/LazerBank","one/two/three/file1");
		Properties props = new Properties();
		props.setProperty("Image Type","..|...");
		da.setCategories(props);
		CommerceProduct p = new CommerceProduct();
		da.setCategoryValues(p);
		assertEquals("[two, three]",p.getValues("Image Type").toString());
		
	}
}
