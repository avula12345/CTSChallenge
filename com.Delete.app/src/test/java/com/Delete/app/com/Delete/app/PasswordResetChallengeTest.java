package com.Delete.app.com.Delete.app;

import java.io.File;
import java.io.IOException;

import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import utility.GenericClasses;

public class PasswordResetChallengeTest {

	WebDriver driver;
	String Path_ScreenShot = "\\Screenshots\\Screenshot_";
	String subject = "Newegg.com - Password Retrieve";
	String url = "https://www.newegg.com/";
	String reguserName = "ctschallenge@gmail.com";
	String password = "challenge2017";

	@BeforeClass
	public void setup() {

		// Launching the URL

		System.out.println("Setup Main test...................");
		System.setProperty("webdriver.gecko.driver", "\\drivers\\geckodriver.exe");
		driver = new FirefoxDriver();
		driver.get(url);
	}

	@Test
	public void pwdResetTest() throws InterruptedException, MessagingException, IOException {

		GenericClasses gc = new GenericClasses();
		String resetPwd = gc.ResetPassword(driver, reguserName, password, subject, url);
		gc.loginWithResetPassword(driver, reguserName, resetPwd);
		gc.addItemsToCart(driver);
	}

	@AfterClass
	public void teardown() throws Exception {

		System.out.println("teardown() in Main test...................");
		String sTestCaseName = "SelfEmpSignUp";
		try {
			File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(scrFile, new File(Path_ScreenShot + "_" + sTestCaseName + ".jpg"));

		} catch (Exception e) {
			throw new Exception();
		}
		driver.quit();
	}
}
