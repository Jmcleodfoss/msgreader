package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.KVPArray;
import io.github.jmcleodfoss.msg.KVPEntry;

import java.util.ArrayList;
import java.util.Iterator;

import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;

class MiniStream extends Tab
{
	static private final String PROPNAME_MINISTREAM_TAB_TITLE = "Ministream";

	/** The parent pane. Left is a single-columned table with the mini
	*   stream chains, right is the content of the chain.
	*/
	private SplitPane containingPane;

	private StackPane listPane;
	private ListView<ArrayList<Integer>> list;
	private StackPane dataPane;
	private ByteDataTable dataDisplay;

	private ArrayList<byte[]> data;

	MiniStream(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_MINISTREAM_TAB_TITLE));

		list = new ListView<ArrayList<Integer>>();
		list.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>(){
			@Override
			public void onChanged(ListChangeListener.Change c)
			{
				while (c.next()){
					if (c.wasReplaced()){
						dataDisplay.update(data.get((Integer)c.getList().get(0)));
					}
				}
			}
		});
		listPane = new StackPane();
		listPane.getChildren().add(list);

		data = new ArrayList<byte[]>();
		dataDisplay = new ByteDataTable(false);
		dataPane = new StackPane();
		dataPane.getChildren().add(dataDisplay);

		containingPane = new SplitPane();
		containingPane.getItems().addAll(listPane, dataPane);
		containingPane.setDividerPositions(0.4f);

		setContent(containingPane);
	}

	void update(MSG msg, LocalizedText localizer)
	{
		if (msg == null){
			list.getItems().clear();
			dataDisplay.clear();
			data.clear();
			return;
		}

		KVPArray<ArrayList<Integer>, byte[]> miniFATData = msg.miniFATData();
		Iterator<KVPEntry<ArrayList<Integer>, byte[]>> iter = miniFATData.iterator();
		while (iter.hasNext()) {
			KVPEntry<ArrayList<Integer>, byte[]> row = iter.next();
			list.getItems().add(row.getKey());
			data.add(row.getValue());
		}
		list.getSelectionModel().selectFirst();
		dataDisplay.clear();
		dataDisplay.update(data.get(0));
	}
}
