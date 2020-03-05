package io.github.jmcleodfoss.msgviewer;

import io.github.jmcleodfoss.msg.MSG;

import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

/** Display raw sectors. */
class Sectors extends Tab
{
	static private final String PROPNAME_SECTOR_TAB_TITLE = "Sectors";

	private ByteDataTable data;
	private StackPane pane;
	private Pagination pagination;

	private MSG msg;

	Sectors(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_SECTOR_TAB_TITLE));

		data = new ByteDataTable(false);
		pane = new StackPane();
		pane.getChildren().add(data);

		pagination = new Pagination();
		pagination.setPageFactory(new Callback<Integer, Node>(){
			@Override
			public Node call(Integer pageIndex)
			{
				return createPage(pageIndex);
			}
		});
		pagination.setSkin(new VoluminousPaginationSkin(pagination));
		setContent(pagination);
	}

	public StackPane createPage(int pageIndex)
	{
		if (msg != null)
			data.update(msg.getSector(pageIndex));
		return pane;
	}

	void update(MSG msg, LocalizedText localizer)
	{
		this.msg = msg;
		pagination.setPageCount(msg.numberOfSectors());
		pagination.setCurrentPageIndex(0);
	}
}
