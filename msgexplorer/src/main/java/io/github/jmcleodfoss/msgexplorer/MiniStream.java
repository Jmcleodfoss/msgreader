package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.KVPArray;
import io.github.jmcleodfoss.msg.KVPEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import javafx.collections.ListChangeListener;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;

/** Tab displaying the mini stream
*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/c5d235f7-b73c-4ec5-bf8d-5c08306cd023">MS-CFB Section 2.4: Compound File Mini FAT Sectors</a>
*/
class MiniStream extends Tab
{
	/* Property for the tab name */
	static private final String TAB_TITLE = "ministream.main.tabname";

	/** The mini stream chains */
	private ListView<List<Integer>> list;

	/** The display for the selected mini stream entry */
	private ByteDataTable dataDisplay;

	/** The data for the selected mini stream entry */
	private List<byte[]> data;

	/** Create the mini stream tab.
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	MiniStream(LocalizedText localizer)
	{
		super(localizer.getText(TAB_TITLE));

		list = new ListView<List<Integer>>();
		list.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>(){
			@Override
			public void onChanged(ListChangeListener.Change<? extends Integer> c)
			{
				while (c.next()){
					if (c.wasReplaced()){
						dataDisplay.update(data.get(c.getList().get(0)));
					}
				}
			}
		});

		StackPane listPane = new StackPane();
		listPane.getChildren().add(list);

		data = new ArrayList<byte[]>();
		dataDisplay = new ByteDataTable();

		StackPane dataPane = new StackPane();
		dataPane.getChildren().add(dataDisplay);

		SplitPane containingPane = new SplitPane();
		containingPane.getItems().addAll(listPane, dataPane);
		containingPane.setDividerPositions(0.4f);

		setContent(containingPane);
	}

	/** Update the mini stream display.
	*	@param	msg	The msg object for the file we are displaying
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	void update(MSG msg, LocalizedText localizer)
	{
		if (msg == null){
			list.getItems().clear();
			dataDisplay.clear();
			data.clear();
			return;
		}

		KVPArray<List<Integer>, byte[]> miniFATData = msg.miniFATData();
		Iterator<KVPEntry<List<Integer>, byte[]>> iter = miniFATData.iterator();
		while (iter.hasNext()) {
			KVPEntry<List<Integer>, byte[]> row = iter.next();
			list.getItems().add(row.getKey());
			data.add(row.getValue());
		}
		list.getSelectionModel().selectFirst();
		dataDisplay.clear();
		dataDisplay.update(data.get(0));
	}
}
