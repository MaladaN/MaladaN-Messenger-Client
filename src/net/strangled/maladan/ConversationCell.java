package net.strangled.maladan;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ConversationCell extends ListCell<MessengerConversation> {
    /**
     * The updateItem method should not be called by developers, but it is the
     * best method for developers to override to allow for them to customise the
     * visuals of the cell. To clarify, developers should never call this method
     * in their code (they should leave it up to the UI control, such as the
     * {@link ListView} control) to call this method. However,
     * the purpose of having the updateItem method is so that developers, when
     * specifying custom cell factories (again, like the ListView
     * {@link ListView#cellFactoryProperty() cell factory}),
     * the updateItem method can be overridden to allow for complete customisation
     * of the cell.
     * <p>
     * <p>It is <strong>very important</strong> that subclasses
     * of Cell override the updateItem method properly, as failure to do so will
     * lead to issues such as blank cells or cells with unexpected content
     * appearing within them. Here is an example of how to properly override the
     * updateItem method:
     * <p>
     * <pre>
     * protected void updateItem(T item, boolean empty) {
     *     super.updateItem(item, empty);
     *
     *     if (empty || item == null) {
     *         setText(null);
     *         setGraphic(null);
     *     } else {
     *         setText(item.toString());
     *     }
     * }
     * </pre>
     * <p>
     * <p>Note in this code sample two important points:
     * <ol>
     * <li>We call the super.updateItem(T, boolean) method. If this is not
     * done, the item and empty properties are not correctly set, and you are
     * likely to end up with graphical issues.</li>
     * <li>We test for the <code>empty</code> condition, and if true, we
     * set the text and graphic properties to null. If we do not do this,
     * it is almost guaranteed that end users will see graphical artifacts
     * in cells unexpectedly.</li>
     * </ol>
     *
     * @param item  The new item for the cell.
     * @param empty whether or not this cell represents data from the list. If it
     *              is empty, then it does not represent any domain data, but is a cell
     *              being used to render an "empty" row.
     * @expert
     */
    @Override
    protected void updateItem(MessengerConversation item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            StackPane mainPane = new StackPane();
            mainPane.setId("convoPane");

            ImageView contactPhoto = new ImageView("file:" + item.getContactPhotoPath());
            contactPhoto.setFitHeight(64);
            contactPhoto.setFitWidth(64);
            contactPhoto.setSmooth(true);

            StackPane photoContainer = new StackPane();
            photoContainer.setMaxHeight(64);
            photoContainer.setMaxWidth(64);
            photoContainer.setId("photoContainer");

            Text username = new Text();
            username.setFill(Color.WHITE);
            username.setText(item.getContactName());


        }

    }
}
