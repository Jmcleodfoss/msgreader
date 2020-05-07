package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.voluminouspaginationskin.VoluminousPaginationSkin;
import io.github.jmcleodfoss.msg.MSG;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

/** Tab displaying raw sector contents.
*   Note that the Pagination control numbers the sectors from 1, although all documentation numbers them from 0.
*/
class Sectors extends Tab
{
	/* Property for the tab name */
	static private final String TAB_TITLE = "sectors.main.tabname";

	/** Utility class to update the display after asynchronous read of a new sector. */
	private class SuccessfulReadHandler implements EventHandler<WorkerStateEvent>
	{
		@Override
		public final void handle(WorkerStateEvent t)
		{
			byte[] sectorData = (byte[])t.getSource().getValue();
			if (sectorData != null){
				data.update(sectorData);
			}
		}
	}

	/** Utility class for asynchronous read of a new sector. */
	private class UpdateInfoService extends Service<byte[]>
	{
		/** The (0-based) index of the sector to read */
		private IntegerProperty pageIndex;
		private IntegerProperty pageIndexProperty()
		{
			if (pageIndex == null)
				pageIndex = new SimpleIntegerProperty();
			return pageIndex;
		}
		public void setPageIndex(int pageIndex)
		{
			pageIndexProperty().set(pageIndex);
		}
		public int getPageIndex()
		{
			return pageIndexProperty().get();
		}

		protected Task<byte[]> createTask()
		{
			return new Task<byte[]>() {
				protected byte[] call()
				{
					return msg.getSector(getPageIndex());
				}
			};
		}
	}

	/** The sector contents */
	private ByteDataTable data;

	/** The container for the sector contents display, which allows selection of the sector to view */
	private Pagination pagination;

	/** Reference to the underlying MSG object */
	private MSG msg;

	/** Asynchronous update object */
	private UpdateInfoService updateInfoService;

	/** Create the sector display tab.
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	Sectors(LocalizedText localizer)
	{
		super(localizer.getText(TAB_TITLE));

		pagination = new Pagination();
		pagination.setPageFactory(new Callback<Integer, Node>(){
			@Override
			public Node call(Integer pageIndex)
			{
				if (msg == null || pageIndex == null)
					return null;
				if (pageIndex < 0 || pageIndex >= pagination.getPageCount())
					return null;
				updateInfoService.setPageIndex(pageIndex);
				updateInfoService.setOnSucceeded(new SuccessfulReadHandler());
				updateInfoService.restart();

				data = new ByteDataTable();
				StackPane pane = new StackPane();
				pane.getChildren().add(data);
				return pane;
			}
		});
		pagination.setSkin(new VoluminousPaginationSkin(pagination));
		updateInfoService = new UpdateInfoService();

		setContent(pagination);
	}

	/** Update the sectpr display.
	*	@param	msg	The msg object for the file we are displaying
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	void update(MSG msg, LocalizedText localizer)
	{
		this.msg = msg;
		pagination.setPageCount(msg.numberOfSectors());
		pagination.setCurrentPageIndex(0);
	}
}
