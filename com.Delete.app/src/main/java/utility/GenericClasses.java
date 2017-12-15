package utility;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

public class GenericClasses {


	String Path_ScreenShot = "\\Screenshots\\Screenshot_";
	WebDriverWait wait;

	public void takeScreenshot(WebDriver driver, String sTestCaseName) throws Exception {

		try {
			File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(scrFile, new File(Path_ScreenShot + "_" + sTestCaseName + ".jpg"));

		} catch (Exception e) {
			throw new Exception();
		}
	}

	public String readGmail(String gmailusername, String gmailPassword,String subjectPattern) throws MessagingException, IOException {

		String resetPwdLink = null;
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");
		Session session = Session.getDefaultInstance(props, null);

		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", gmailusername, gmailPassword);

		Folder inbox = store.getFolder("inbox");
		inbox.open(Folder.READ_WRITE);
		int messageCount = inbox.getMessageCount();

		System.out.println("Total Messages:- " + messageCount);

		Message[] messages = inbox.getMessages();
		int count = messages.length;
		System.out.println("------------------------------");
		for (int i = 0; i < count; i++) {
			Message message = messages[i];
			if (messages[i].getSubject().equals(subjectPattern)) {
				String body = (String) messages[i].getContent();
				System.out.println(body);
				String start = "reset&nbsp;your password.<br><br><a";
				String end = "target=_blank>Reset Password Link<";
				int from = body.indexOf(start) + start.length();
				int to = body.indexOf(end);
				String finalStr = body.substring(from, to).split("<a href=")[0].trim();
				resetPwdLink = finalStr.split("\"")[1];
				System.out.println(resetPwdLink);
				message.setFlag(Flags.Flag.DELETED, true);
				break;
			}
		}
		return resetPwdLink;
	}
	
	public String ResetPassword(WebDriver driver, String emailAddress,String password,String subject, String url) throws MessagingException, IOException{
		
		System.out.println("pwdResetTest() ...................");
		
		String newPwd = null;
		// Check that Login link located in the Main Page
		wait = new WebDriverWait(driver, 10);
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='usaSite']/a")));
		boolean loginLink = element.isDisplayed();
		System.out.println(loginLink);
		Assert.assertTrue(loginLink);
		System.out.println("LoginLink displayed = " +loginLink);
		
		if(loginLink){
			
			System.out.println("Element Displayed");
			
			//Click on the Login Link
			driver.findElement(By.xpath("//*[@id='usaSite']/a")).click();
			
			//Verify that Username Text field exists or not
			WebElement userName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("UserName")));
			boolean uNameDis = userName.isDisplayed();
			System.out.println("UserName Field displayed = " +uNameDis);
			Assert.assertTrue(uNameDis);
			
			//Click on the Forgot Password link
			if(uNameDis) {
				WebElement forgotPwd = driver.findElement(By.xpath("//span/a[@title='Forgot your password?']"));
				forgotPwd.click();
			} else {
				Assert.assertTrue(uNameDis);
			}
			
			WebElement forgotPwduserName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("loginname")));
			boolean loginName = forgotPwduserName.isDisplayed();
			System.out.println("Email Address Field displayed = " +loginName);
			Assert.assertTrue(loginName);
			
			//Click on the Submit button after entering the username
			if(loginName) {
				WebElement emailAdd = driver.findElement(By.xpath("//input[@name='loginname']"));
				emailAdd.sendKeys(emailAddress);
				WebElement submitBtn = driver.findElement(By.xpath("//form[@name='frmLogAssistByEmail']/table/tbody/tr[6]/td/input"));
				submitBtn.click();
			} else {
				Assert.assertTrue(loginName);
			}
			
			//Verify that Email reset password acknowledgement displayed or not
			WebElement emailPwdResetAck = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='QuickLinks']/dt")));
			boolean emailPwdResetAckIsDisplayed = emailPwdResetAck.isDisplayed();
			System.out.println("Email Password Acknowledgement displayed = " +emailPwdResetAckIsDisplayed);
			Assert.assertTrue(emailPwdResetAckIsDisplayed);
			
			//Connect to GMAIL and Find the email based on the subject and get the reset password link
			
			if(emailPwdResetAckIsDisplayed) {
			
				String resetPwdLink =  readGmail(emailAddress,password,subject);
				System.out.println("Reset Password Link = " + resetPwdLink);
				
				//Open the Reset Password link
				driver.get(resetPwdLink);	
				
				WebElement createPwd = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@class='atnSecondary']")));
				boolean pwdReset = createPwd.isDisplayed();
				System.out.println(pwdReset);
				Assert.assertTrue(pwdReset);
	
				// Always create new password using timeIn Milliseconds with some constant string
				Calendar calendar = Calendar.getInstance();
				long timeMillisec = calendar.getTimeInMillis();
				newPwd = "Challenge@" + timeMillisec;
				System.out.println("New Password = " +newPwd);
				
				//Sending the password and clicking on Create Password Button
				if(pwdReset) {
					WebElement pwd_1 = driver.findElement(By.id("newpassword"));
					pwd_1.sendKeys(newPwd);
					WebElement pwd_2 = driver.findElement(By.id("newpassword1"));
					pwd_2.sendKeys(newPwd);
					WebElement createPwdBtn = driver.findElement(By.xpath("//a[@class='atnSecondary']"));
					createPwdBtn.click();
					
					//Validating that acknowledgement is displayed after resetting the password
					WebElement pwdResetAck = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='infoContent']/h1")));
					boolean acknowledgeMsg = pwdResetAck.isDisplayed();
					System.out.println(acknowledgeMsg);
					Assert.assertTrue(acknowledgeMsg);
					
					String message = pwdResetAck.getText();
					System.out.println(message);
					
				} else {
					Assert.assertTrue(pwdReset);
				}
			}
		
		}
		return newPwd;
	}

	public void loginWithResetPassword(WebDriver driver,String reguserName, String resetPwd) {
		
		System.out.println("Login with Reset password...................");
		
		// Check that Login link located in the Main Page
		wait = new WebDriverWait(driver, 10);
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='usaSite']/a")));
		boolean loginLink = element.isDisplayed();
		System.out.println(loginLink);
		Assert.assertTrue(loginLink);
		System.out.println("LoginLink displayed = " +loginLink);
		
		if(loginLink){
			
			System.out.println("Element Displayed");
			
			//Click on the Login Link
			driver.findElement(By.xpath("//*[@id='usaSite']/a")).click();
			WebElement emailAdd = driver.findElement(By.id("UserName"));
			emailAdd.sendKeys(reguserName);
			WebElement password = driver.findElement(By.id("UserPwd"));
			password.sendKeys(resetPwd);
			
			WebElement submit = driver.findElement(By.id("submit"));
			submit.click();
			
			WebElement afterLogin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='usaSite']/a/ins")));
			boolean successVal = afterLogin.isDisplayed();
			System.out.println(successVal);
			Assert.assertTrue(successVal);
			
			String messageConf = afterLogin.getText();
			System.out.println(messageConf);
			
			if(messageConf.equalsIgnoreCase("My Account")) {
				
				System.out.println("User Successfully Logged In");
				Assert.assertTrue(true);
			} else {
				System.out.println("User Login Failed");
				Assert.assertTrue(false);
			}
		
		}
	}
	
	public void addItemsToCart(WebDriver driver) throws InterruptedException{
		
			WebDriverWait wait = new WebDriverWait(driver, 10);;
			WebElement searchBoxVal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='haQuickSearchBox']")));
			boolean sbVal = searchBoxVal.isDisplayed();
			System.out.println(sbVal);
			Assert.assertTrue(sbVal);
			searchBoxVal.click();
			searchBoxVal.clear();
			searchBoxVal.sendKeys("Computer Speakers");
			WebElement searchBtn = driver.findElement(By.xpath("//button[text()='Search']"));
			searchBtn.click();
			Thread.sleep(5000);
			WebElement val = driver.findElement(By.xpath("//a[@title='Speakers']/following-sibling::*"));
			String count = val.getText();	
			String countVal = count.split("\\(")[1].split("\\+")[0];
			System.out.println("Total Count = " +Integer.parseInt(countVal));	
			
			if(Integer.parseInt(countVal)>0) {
				
				WebElement selectItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='items-view is-grid']/div[1]/a")));
				selectItem.click();
				
				WebElement addtoCart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='landingpage-cart']/div/div[2]/button")));
				addtoCart.click();
					
				WebElement popupAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='centerPopup-body']")));
				if(popupAlert.isDisplayed()) {
					
					WebElement addtoCartbtn = driver.findElement(By.xpath("//button[text()='Add to cart']"));
					addtoCartbtn.click();
				} else {
					Assert.assertTrue(false);
				}
				
			}
		
	}
}