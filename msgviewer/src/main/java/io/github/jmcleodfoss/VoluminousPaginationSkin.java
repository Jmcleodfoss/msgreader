package io.github.jmcleodfoss.msgviewer;

import com.sun.javafx.scene.control.skin.PaginationSkin;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Pagination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

/**
 *  A pagination skin with additional controls for use with paginations which
 *  must display a large number of pages. It adds the following controls to
 *  the navigation section:
 *  <ul>
 *  	<li>\\u23ee, Jump to first page</li>
 *  	<li>\\u23ea, Jump backward by 10%</li>
 *  	<li>\\u23a9, Jump forward by 10%</li>
 *  	<li>\\u23ed, Jump to last page</li>
 *  </ul>
 *
 *  This is for JavaFX 8. Later versions should extend
 *  javafx.scene.control.skin.PaginationSkin 
 *
 *  This was inspired by @see <a href="https://stackoverflow.com/questions/31540001">Stack Overflow Question 31540001</a>
 *  with code copied from @see <s href="github.com/openjdk/jfx/blob/master/modulres/javafx.controls/src/main/java/javafx/scene/control/skin/PaginationSkin.java>OpenJDK implementation of PagnitionSkin</a>
 *
 *  CSS (including arrow shape definitions) are in @link{resources/css/VoluminousPaginationSkin.css}
 */
class VoluminousPaginationSkin extends PaginationSkin
{
	/** The relative distance by which to jump for jumpBackward and jumpForward */
	private static final double JUMP_FRACTION = 0.10;

	/** The container for all the navigation controls, constructed by the
	*   PaginationSkin constructor.
	*/
	private final HBox controlBox;

	/** The "next" arrow in the set of navigation controls. This is the last
	*   button created in this section by the parent class constructor, and
	*   is used to trigger addition of the new buttons when needed.
	*/
	private final Button nextArrowButton;

	/** The button to jump to the first page */
	private Button firstArrowButton;

	/** The button to jump backward by (Number of pages) * JUMP_FRACTION */
	private Button jumpBackwardButton;

	/** The button to jump forward by (Number of pages) * JUMP_FRACTION */
	private Button jumpForwardButton;

	/** The button to jump to the last page */
	private Button lastArrowButton;

	/** Create a paginator skin for the given pagination. */
	VoluminousPaginationSkin(final Pagination pagination)
	{
		super(pagination);

		Node control = pagination.lookup(".control-box");
		assert control instanceof HBox;
		controlBox = (HBox)control;

		nextArrowButton = (Button)controlBox.getChildren().get(controlBox.getChildren().size()-1);
		double minButtonSize = nextArrowButton.getMinWidth();

		firstArrowButton = createNavigationButton("first-arrow", "first-arrow-button", minButtonSize);
		firstArrowButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent t)
			{
				pagination.setCurrentPageIndex(0);
			}
		});
		firstArrowButton.disableProperty().bind(pagination.currentPageIndexProperty().isEqualTo(0));

		jumpBackwardButton = createNavigationButton("jump-backward-arrow", "jump-backward-arrow-button", minButtonSize);
		jumpBackwardButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent t)
			{
				int currentPage = pagination.getCurrentPageIndex();
				int target = Math.max(0, currentPage - jumpDistance());
				pagination.setCurrentPageIndex(target);
			}
		});
		jumpBackwardButton.disableProperty().bind(pagination.currentPageIndexProperty().lessThan(pagination.pageCountProperty().multiply(JUMP_FRACTION)));

		jumpForwardButton = createNavigationButton("jump-forward-arrow", "jump-forward-arrow-button", minButtonSize);
		jumpForwardButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent t)
			{
				int currentPage = pagination.getCurrentPageIndex();
				int maxPage = pagination.getPageCount();
				int target = Math.min(maxPage, currentPage + jumpDistance());
				pagination.setCurrentPageIndex(target);
			}
		});
		jumpForwardButton.disableProperty().bind(pagination.currentPageIndexProperty().greaterThan(pagination.pageCountProperty().multiply(1.0 - JUMP_FRACTION)));

		lastArrowButton = createNavigationButton("last-arrow", "last-arrow-button", minButtonSize);
		lastArrowButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent t)
			{
				pagination.setCurrentPageIndex(pagination.getPageCount());
			}
		});
		lastArrowButton.disableProperty().bind(pagination.currentPageIndexProperty().isEqualTo(pagination.pageCountProperty().subtract(1)));

		controlBox.getChildren().addListener(new ListChangeListener(){
			@Override
			public void onChanged(ListChangeListener.Change c)
			{
				while (c.next()){
					if (	c.wasAdded()
					   && 	!c.wasRemoved()
					   &&	c.getAddedSize() == 1
					   &&   c.getAddedSubList().get(0) == nextArrowButton
					   &&  !controlBox.getChildren().contains(jumpBackwardButton)){
						updateNavigation();
					}
				}
			}
		});

		updateNavigation();
	}

	/** Create a navigation button.
 	*	@param	graphicCSSClass	The CSS class to use for the graphic
 	*				displayed in the button
 	*	@param	buttonCSSClass	The CSS class used for the button
 	*	@param	minButtonSize	The minimum button size used by all
 	*				buttons in the navigation container
 	*	@return	A button with the given styles associated with it.
 	*/
	private Button createNavigationButton(String graphicCSSClass, String buttonCSSClass, double minButtonSize)
	{
		StackPane pane = new StackPane();
		pane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		pane.getStyleClass().add(graphicCSSClass);

		Button button = new Button("");
		button.setMinSize(minButtonSize, minButtonSize);
		button.prefWidthProperty().bind(button.minWidthProperty());
		button.prefHeightProperty().bind(button.minWidthProperty());
		button.getStyleClass().add(buttonCSSClass);
		button.setFocusTraversable(false);
		button.setGraphic(pane);

		return button;
	}

	/** Calculate the distance to jump by based on the number of pages.
	*	@return	The distance to jump.
	*	@see JUMP_FRACTION
	*/
	private int jumpDistance()//Pagination pagination)
	{
		Pagination pagination = getSkinnable();
		int maxPage = pagination.getPageCount();
		return (int)(maxPage * JUMP_FRACTION);
	}

	/** Add the new navigation buttons to the navigation container */
	private void updateNavigation()
	{
		controlBox.getChildren().add(0, jumpBackwardButton);
		controlBox.getChildren().add(0, firstArrowButton);
		controlBox.getChildren().add(jumpForwardButton);
		controlBox.getChildren().add(lastArrowButton);
	}
}
