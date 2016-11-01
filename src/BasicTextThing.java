package attempt1;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Iterator;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Basic Text Editor, similar to Notepad, but with less features and probably more buggy.
 * @author Jordan
 *
 */
public class BasicTextThing extends Application
{
	private Scene scene = null;
	private Stage primaryStage;
	private TextArea text = null;
	private BorderPane root = null;
	private String openFilename = null;
	private static boolean ignoreNextPress = false;
	private Label filename = new Label("current file: --new file--");	//defualt filename
	private FileChooser.ExtensionFilter extFilterTXT = new FileChooser.ExtensionFilter("text files (*.txt)", "*.txt");	//supported file formats
	
	/*
	 * (non-Javadoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 * 
	 * Sets up BorderPane, menu, text area and what happens on shortcuts
	 */
	@Override
	public void start(Stage primaryStage)
	{
		root = new BorderPane();
		scene = new Scene(root, 800, 600);
		scene.setOnKeyPressed(new EventHandler<KeyEvent>()
				{
					public void handle(KeyEvent ke)
					{
						shortcuts(ke);
					}
				});
		scene.setOnKeyReleased(new EventHandler<KeyEvent>()
				{
					public void handle(KeyEvent ke)
					{
						String text = ke.getText();
						if(!(ke.isControlDown() || ke.isMetaDown()))
						{
							if(text.equalsIgnoreCase("s") 
								||text.equalsIgnoreCase("o")
								||text.equalsIgnoreCase("n") 
								&& ignoreNextPress)
							{
								ignoreNextPress = false;
								return;
							}
							shortcuts(ke);
						}
					}
				});
		//Create menu bar
		MenuBar menuBar = new MenuBar();
		
		//File menu
		Menu fileMenu = new Menu("file");
		MenuItem newMenuItem = new MenuItem("new");
		newMenuItem.setOnAction(new EventHandler<ActionEvent>()
				{
					public void handle(ActionEvent event)
					{
						root.setBottom(newFile());
						renameNotes();
					}
				});
		MenuItem openMenuItem = new MenuItem("open");
		openMenuItem.setOnAction(new EventHandler<ActionEvent>()
				{
					public void handle(ActionEvent event)
					{
						openFile();
						renameNotes();
					}
				});
		MenuItem saveMenuItem = new MenuItem("save");
		saveMenuItem.setOnAction(new EventHandler<ActionEvent>()
				{
					public void handle(ActionEvent event)
					{
						saveFile();
						renameNotes();
					}
				});
		MenuItem exitMenuItem = new MenuItem("exit");
		exitMenuItem.setOnAction(new EventHandler<ActionEvent>()
				{
					public void handle(ActionEvent event)
					{
						try 
						{
							stop();
							System.exit(0);
						} 
						catch (Exception e) 
						{
							e.printStackTrace();
						}
					}
				});
		//Add items to file menu	
		fileMenu.getItems().addAll(
				newMenuItem, 
				openMenuItem, 
				saveMenuItem, 
				new SeparatorMenuItem(), 
				exitMenuItem);
		
		//Edit menu
		Menu editMenu = new Menu("edit");
		MenuItem copyMenuItem = new MenuItem("copy");
		copyMenuItem.setOnAction(new EventHandler<ActionEvent>()
				{
					public void handle(ActionEvent event)
					{
						text.copy();
					}
				});
		MenuItem cutMenuItem = new MenuItem("cut");
		cutMenuItem.setOnAction(new EventHandler<ActionEvent>()
				{
					public void handle(ActionEvent event)
					{
						text.cut();
					}
				});
		MenuItem pasteMenuItem = new MenuItem("paste");
		pasteMenuItem.setOnAction(new EventHandler<ActionEvent>()
				{
					public void handle(ActionEvent event)
					{
						text.paste();
					}
				});
		MenuItem selectAllMenuItem = new MenuItem("select");
		selectAllMenuItem.setOnAction(new EventHandler<ActionEvent>()
				{
					public void handle(ActionEvent event)
					{
						text.selectAll();
					}
				});
		//Add items to edit menu
		editMenu.getItems().addAll(
				copyMenuItem, 
				cutMenuItem, 
				pasteMenuItem,
				new SeparatorMenuItem(),
				selectAllMenuItem);
		
		
		//View Menu
		Menu viewMenu = new Menu("view");
		Menu setWrapMenu = new Menu("change text wrapping");
		MenuItem wrapOnMenuItem = new MenuItem("text wrapping on");
		wrapOnMenuItem.setOnAction(new EventHandler<ActionEvent>()
				{
					public void handle(ActionEvent event)
					{
						text.setWrapText(true);
					}
				});
		MenuItem wrapOffMenuItem = new MenuItem("text wrapping off");
		wrapOffMenuItem.setOnAction(new EventHandler<ActionEvent>()
				{
					public void handle(ActionEvent event)
					{
						text.setWrapText(false);
					}
				});
		//Add items to wrap menu
		setWrapMenu.getItems().addAll(wrapOnMenuItem, wrapOffMenuItem);
		//Add items to view menu
		viewMenu.getItems().add(setWrapMenu);
		//Add menus to menu bar
		menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu);
		
		//TextArea
		text = new TextArea();
		text.setWrapText(true);		//stops it from going off the edge as default
		root.setTop(filename);
		root.setBottom(text);
		text.setPrefHeight(root.getHeight());
		text.setPrefWidth(root.getWidth());
		root.setCenter(menuBar);
		primaryStage.setTitle("thoughtpad");
		primaryStage.setScene(scene);
		primaryStage.show();
		scene.getStylesheets().add("style.css");
	}
	
	/*
	 * Method handling new Files
	 * Allows user to save if they have already started typing
	 */
	private TextArea newFile()
	{
		if(text.getText() != null)
		{
			saveFile();
			return text;		//if user does not save, return the same text area 
		}
		openFilename = null;
		text = new TextArea();
		text.setPrefHeight(root.getHeight());
		text.setPrefWidth(root.getWidth());
		return text;	
	}
	
	/*
	 * Method handling opening of Files
	 */
	private  void  openFile()
	{
		try 
		{
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(extFilterTXT);
			fc.setTitle("select file to open");
			File selectedFile = fc.showOpenDialog(null);
			openFilename = selectedFile.getAbsolutePath();
			if(selectedFile != null)
			{
			  StringBuffer sb = new StringBuffer();
			   try(FileInputStream fis = new FileInputStream(selectedFile);
			        BufferedInputStream bis = new BufferedInputStream(fis))
			   {
			       while ( bis.available() > 0 ) 
			       {
			    	   sb.append((char)bis.read());
			    	}
			   }
			    catch ( Exception e ) 
			    {
			        e.printStackTrace();
			    }
			   text.setText(sb.toString());
			}
		} 
		catch (NullPointerException e)
		{
			//if no file is selected, do nothing
		}
	}
	
	/*
	 * Method handling saving of files
	 */
	private  void saveFile()
	{
		File file = null;
		if(openFilename == null)
		{
			//User made new file - just started typing
			FileChooser fc = new FileChooser();
			fc.setTitle("select location to save");
			fc.getExtensionFilters().add(extFilterTXT);
			File newFile = fc.showSaveDialog(null);
			if(newFile != null)
			{
				//Check if has file extension
				if(! newFile.getName().contains("."))
				{
					String newFilePath = newFile.getAbsolutePath();
					newFilePath += ".txt";
					newFile.delete();
					newFile = new File(newFilePath);
				}
				file = newFile;
				openFilename = newFile.getAbsolutePath();
			}
			else
			{
				if(openFilename == null)
				{
					return;	//if FileChooser was exited, don't try to create new file with null, just exit method
				}
				file = new File(openFilename);
			}
		}
		
		//Write to file
		ObservableList<CharSequence> paragraphs = text.getParagraphs();
		Iterator<CharSequence> iterator = paragraphs.iterator();
		try
		{
			BufferedWriter bf = new BufferedWriter(new FileWriter(file));
			while(iterator.hasNext())
			{
				CharSequence seq = iterator.next();
				bf.append(seq);
				bf.newLine();
			}
			bf.flush();
			bf.close();
		}
		catch(NullPointerException e)
		{
			System.out.println("Null pointer: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		catch(Exception e)
		{
			System.out.println("File save failed | error: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	/*
	 * Method for naming the current file label  in the editor
	 */
	private  void renameNotes()
	{
		String filePath = openFilename;
		if(filePath != null)
		{
			filename.setText("current file: " + filePath.toLowerCase());
			return;
		}
		filename.setText("current file: --new file--");
	}
	
	/*
	 * Method handling shortcuts
	 */
	private  void shortcuts(KeyEvent ke)
	{
		boolean ctrl = false;
		String text = ke.getText();
		if(ke.isControlDown() || ke.isMetaDown())
		{
			ctrl = true;
		}
		if(ctrl && text.equalsIgnoreCase("s"))
		{
			saveFile();
			renameNotes();
			ignoreNextPress = true;
		}
		else if(ctrl && text.equalsIgnoreCase("n"))
		{
			root.setBottom(newFile());
			renameNotes();
			ignoreNextPress = true;
		}
		else if(ctrl && text.equalsIgnoreCase("o"))
		{
			openFile();
			renameNotes();
			ignoreNextPress = true;
		}
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
}
