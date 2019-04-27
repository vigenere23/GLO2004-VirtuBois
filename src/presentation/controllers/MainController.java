package presentation.controllers;

import domain.dtos.BundleDto;
import domain.dtos.LiftDto;
import enums.EditorMode;
import helpers.FileHelper;
import helpers.JavafxHelper;
import helpers.Point2D;
import helpers.UndoRedo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import presentation.presenters.ElevationViewPresenter3D;
import presentation.presenters.YardPresenter;

import javax.sound.sampled.Line;
import java.time.LocalTime;
import java.util.List;

public class MainController extends BaseController {

    public ObjectProperty<EditorMode> editorMode;
    public ToggleGroup editorModeToggleGroup;
    public boolean gridIsOn;
    public char elevationViewMode;

    private DropShadow dropShadow;
    private ElevationViewPresenter3D elevationViewPresenter3D;
    private YardPresenter yardPresenter;

    private List<BundleDto> observableBundleList;
    private BundleDto selectedBundle;

    @FXML
    Pane root;
    @FXML
    Pane yardWrapper;

    @FXML
    public Button undoButton;
    @FXML
    public ImageView undoImage;
    @FXML
    public Button redoButton;
    @FXML
    public ImageView redoImage;

    @FXML
    public TextField inventorySearchBar;
    @FXML
    public TableView<BundleDto> inventoryTable;
    @FXML
    public TableColumn<BundleDto, String> codeColumn;
    @FXML
    public TableColumn<BundleDto, String> typeColumn;
    @FXML
    public TableColumn<BundleDto, String> sizeColumn;

    @FXML
    public TextField bundleBarcodeValue;
    @FXML
    public TextField bundleLengthValue;
    @FXML
    public TextField bundleWidthValue;
    @FXML
    public TextField bundleHeightValue;
    @FXML
    public DatePicker bundleDateValue;
    @FXML
    public Spinner<Integer> bundleHourValue;
    @FXML
    public Spinner<Integer> bundleMinuteValue;
    @FXML
    public TextField bundleEssenceValue;
    @FXML
    public TextField bundlePlankSizeValue1;
    @FXML
    public TextField bundlePlankSizeValue2;
    @FXML
    public TextField bundleXPosValue;
    @FXML
    public TextField bundleYPosValue;
    @FXML
    public Label bundleZPosValue;
    @FXML
    public TextField bundleAngleValue;

    @FXML
    public TextField armsHeightValue;
    @FXML
    public Label armsXValue;
    @FXML
    public Label armsYValue;
    @FXML
    public Label liftHeightValue;
    @FXML
    public TextField liftXValue;
    @FXML
    public TextField liftYValue;
    @FXML
    public TextField liftAngleValue;

    @FXML
    public ToggleButton pointerButton;
    @FXML
    public ToggleButton addBundleButton;
    @FXML
    public ToggleButton deleteButton;
    @FXML
    public ToggleButton snapGridButton;

    @FXML
    public AnchorPane subSceneAnchor;
    @FXML
    public StackPane subScenePane;


    @FXML
    public void initialize() {
        root.setFocusTraversable(false);
        editorMode = new SimpleObjectProperty<>();

        initTableView();
        initInventorySearchBar();
        setEventHandlers();
        setupEditorModeToggleButtons();
        initYard();
        initElevationView();
        initBundleInfo();
        initLiftInfo();

        dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setColor(Color.GREY);
        gridIsOn = false;
        elevationViewMode = 'x';
    }

    private void setEventHandlers() {
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                editorMode.setValue(EditorMode.POINTER);
            }
        });
    }

    private void setupEditorModeToggleButtons() {
        snapGridButton.setOnAction(event -> {
            gridIsOn = snapGridButton.isSelected();
            yardPresenter.draw();
        });
        pointerButton.setOnAction(event -> editorMode.setValue(EditorMode.POINTER));
        addBundleButton.setOnAction(event -> editorMode.setValue(EditorMode.ADDING_BUNDLE));
        deleteButton.setOnAction(event -> editorMode.setValue(EditorMode.DELETE));

        editorModeToggleGroup = new ToggleGroup();
        pointerButton.setToggleGroup(editorModeToggleGroup);
        addBundleButton.setToggleGroup(editorModeToggleGroup);
        deleteButton.setToggleGroup(editorModeToggleGroup);

        editorMode.addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(EditorMode.POINTER)) editorModeToggleGroup.selectToggle(pointerButton);
            else if (newValue.equals(EditorMode.ADDING_BUNDLE)) editorModeToggleGroup.selectToggle(addBundleButton);
            else if (newValue.equals(EditorMode.DELETE)) editorModeToggleGroup.selectToggle(deleteButton);
            else editorModeToggleGroup.selectToggle(null);
        });

        editorMode.setValue(EditorMode.POINTER);
    }

    private void initYard() {
        yardPresenter = new YardPresenter(this);
        yardWrapper.getChildren().setAll(yardPresenter);
        AnchorPane.setRightAnchor(yardPresenter, 0.0);
        AnchorPane.setLeftAnchor(yardPresenter, 0.0);
        AnchorPane.setBottomAnchor(yardPresenter, 0.0);
        AnchorPane.setTopAnchor(yardPresenter, 0.0);
    }

    private void initElevationView() {
        elevationViewPresenter3D = new ElevationViewPresenter3D(subScenePane, this);
    }

    private void initBundleInfo() {
        JavafxHelper.addStringToDoubleConverter(bundleLengthValue, null, 0.0, null);
        JavafxHelper.addStringToDoubleConverter(bundleWidthValue, null, 0.0, null);
        JavafxHelper.addStringToDoubleConverter(bundleHeightValue, null, 0.0, null);
        bundleHourValue.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        bundleHourValue.getValueFactory().setValue(0);
        bundleMinuteValue.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        bundleMinuteValue.getValueFactory().setValue(0);
        JavafxHelper.addStringToIntegerConverter(bundlePlankSizeValue1, 0, 1, null);
        JavafxHelper.addStringToIntegerConverter(bundlePlankSizeValue2, 0, 1, null);
        JavafxHelper.addStringToDoubleConverter(bundleXPosValue, 0.0, null, null);
        JavafxHelper.addStringToDoubleConverter(bundleYPosValue, 0.0, null, null);
        JavafxHelper.addStringToDoubleConverter(bundleAngleValue, 0.0, -360.0, 360.0);
        initTextFieldsHandlers();
    }

    private void initLiftInfo() {
        armsHeightValue.setText(String.valueOf(larmanController.getYard().getLift().getArmsHeight()));
        liftHeightValue.setText(String.valueOf(larmanController.getYard().getLift().getHeight()));
        armsXValue.setText(String.valueOf(larmanController.getYard().getLift().getArmsPosition().getX()));
        armsYValue.setText(String.valueOf(larmanController.getYard().getLift().getArmsPosition().getY()));
        liftXValue.setText(String.valueOf(larmanController.getYard().getLift().getPosition().getX()));
        liftYValue.setText(String.valueOf(larmanController.getYard().getLift().getPosition().getY()));
        liftAngleValue.setText(String.valueOf(larmanController.getYard().getLift().getAngle()));
        initTextFieldsHandlers();
    }

    private void initTextFieldsHandlers() {
        liftAngleValue.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                if(!liftAngleValue.getText().isEmpty()){
                    LiftDto liftDto = new LiftDto(larmanController.getYard().getLift());
                    liftDto.angle = Double.parseDouble(liftAngleValue.getText());
                    larmanController.modifyLiftProperties(liftDto);
                    yardPresenter.draw();
                }
            }
        });
        armsHeightValue.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if(!armsHeightValue.getText().isEmpty()) {
                    LiftDto liftDto = new LiftDto(larmanController.getYard().getLift());
                    liftDto.armsHeight = Double.parseDouble(armsHeightValue.getText());
                    larmanController.modifyLiftProperties(liftDto);
                    yardPresenter.draw();
                }
            }
        });

        liftYValue.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if(!liftYValue.getText().isEmpty()) {
                    LiftDto liftDto = new LiftDto(larmanController.getYard().getLift());
                    liftDto.position = new Point2D(liftDto.position.getX(), Double.parseDouble(liftYValue.getText()));
                    larmanController.modifyLiftProperties(liftDto);
                    yardPresenter.draw();
                }
            }
        });

        liftXValue.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if(!liftXValue.getText().isEmpty()) {
                    LiftDto liftDto = new LiftDto(larmanController.getYard().getLift());
                    liftDto.position = new Point2D(Double.parseDouble(liftXValue.getText()), liftDto.position.getY());
                    larmanController.modifyLiftProperties(liftDto);
                    yardPresenter.draw();
                }
            }
        });
        bundleBarcodeValue.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if (selectedBundle != null) {
                    if (!bundleBarcodeValue.getText().isEmpty()) {
                        selectedBundle.barcode = bundleBarcodeValue.getText();
                        larmanController.modifyBundleProperties(selectedBundle);
                        //elevationViewPresenter.setBundles(selectedBundle);
                        setFocusedBundleElevView(selectedBundle);
                        yardPresenter.draw();
                    }
                    updateBundleInfo(selectedBundle);
                }
            }
        });

        bundleLengthValue.setOnKeyPressed(event -> {
            if (bundleLengthValue.isEditable()) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    if (selectedBundle != null) {
                        if (!bundleLengthValue.getText().isEmpty() && !bundleLengthValue.getText().equals("-") && !bundleLengthValue.getText().equals(".") && !bundleLengthValue.getText().equals("-.") && Double.parseDouble(bundleLengthValue.getText()) != 0.0) {
                            selectedBundle.length = Double.parseDouble(bundleLengthValue.getText());
                            larmanController.modifyBundleProperties(selectedBundle);
                            //elevationViewPresenter.setBundles(selectedBundle);
                            setFocusedBundleElevView(selectedBundle);
                            yardPresenter.draw();
                        }
                        updateBundleInfo(selectedBundle);
                    }

                }
            }
        });

        bundleWidthValue.setOnKeyPressed(event -> {
            if (bundleWidthValue.isEditable()) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    if (selectedBundle != null) {
                        if (!bundleWidthValue.getText().isEmpty() && !bundleWidthValue.getText().equals("-") && !bundleWidthValue.getText().equals(".") && !bundleWidthValue.getText().equals("-.") && Double.parseDouble(bundleWidthValue.getText()) != 0.0) {
                            selectedBundle.width = Double.parseDouble(bundleWidthValue.getText());
                            larmanController.modifyBundleProperties(selectedBundle);
                            //elevationViewPresenter.setBundles(selectedBundle);
                            setFocusedBundleElevView(selectedBundle);
                            yardPresenter.draw();
                        }
                        updateBundleInfo(selectedBundle);
                    }
                }
            }
        });

        bundleHeightValue.setOnKeyPressed(event -> {
            if (bundleHeightValue.isEditable()) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    if (selectedBundle != null) {
                        if (!bundleHeightValue.getText().isEmpty() && !bundleHeightValue.getText().equals("-") && !bundleHeightValue.getText().equals(".") && !bundleHeightValue.getText().equals("-.") && Double.parseDouble(bundleHeightValue.getText()) != 0.0) {
                            selectedBundle.height = Double.parseDouble(bundleHeightValue.getText());
                            larmanController.modifyBundleProperties(selectedBundle);
                            yardPresenter.draw();
                            //elevationViewPresenter.setBundles(selectedBundle);
                            setFocusedBundleElevView(selectedBundle);
                        }
                        updateBundleInfo(selectedBundle);
                    }
                }
            }
        });

        bundleDateValue.setOnAction(event -> {
            if (selectedBundle != null) {
                if (bundleDateValue.getValue() != null) {
                    if (bundleDateValue.getValue() != selectedBundle.date) {
                        selectedBundle.date = bundleDateValue.getValue();
                        larmanController.modifyBundleProperties(selectedBundle);
                        //elevationViewPresenter.setBundles(selectedBundle);
                        setFocusedBundleElevView(selectedBundle);
                        yardPresenter.draw();
                        updateBundleInfo(selectedBundle);
                    }
                }
            }
        });

        bundleHourValue.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if (selectedBundle != null) {
                    selectedBundle.time = LocalTime.of(bundleHourValue.getValue(), bundleMinuteValue.getValue());
                    larmanController.modifyBundleProperties(selectedBundle);
                    //elevationViewPresenter.setBundles(selectedBundle);
                    setFocusedBundleElevView(selectedBundle);
                    yardPresenter.draw();
                    updateBundleInfo(selectedBundle);
                }
            }
        });

        bundleMinuteValue.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if (selectedBundle != null) {
                    selectedBundle.time = LocalTime.of(bundleHourValue.getValue(), bundleMinuteValue.getValue());
                    larmanController.modifyBundleProperties(selectedBundle);
                    //elevationViewPresenter.setBundles(selectedBundle);
                    setFocusedBundleElevView(selectedBundle);
                    yardPresenter.draw();
                    updateBundleInfo(selectedBundle);
                }
            }
        });

        bundleEssenceValue.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if (selectedBundle != null) {
                    if (!bundleEssenceValue.getText().isEmpty()) {
                        selectedBundle.essence = bundleEssenceValue.getText();
                        larmanController.modifyBundleProperties(selectedBundle);
                        //elevationViewPresenter.setBundles(selectedBundle);
                        setFocusedBundleElevView(selectedBundle);
                        yardPresenter.draw();
                    }
                    updateBundleInfo(selectedBundle);
                }
            }
        });

        bundlePlankSizeValue1.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if (selectedBundle != null) {
                    if (!bundlePlankSizeValue1.getText().isEmpty() && !bundlePlankSizeValue2.getText().isEmpty()) {
                        selectedBundle.plankSize = bundlePlankSizeValue1.getText() + "x" + bundlePlankSizeValue2.getText();
                        larmanController.modifyBundleProperties(selectedBundle);
                        //elevationViewPresenter.setBundles(selectedBundle);
                        setFocusedBundleElevView(selectedBundle);
                        yardPresenter.draw();
                    }
                    updateBundleInfo(selectedBundle);
                }
            }
        });

        bundlePlankSizeValue2.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if (selectedBundle != null) {
                    if (!bundlePlankSizeValue1.getText().isEmpty() && !bundlePlankSizeValue2.getText().isEmpty()) {
                        selectedBundle.plankSize = bundlePlankSizeValue1.getText() + "x" + bundlePlankSizeValue2.getText();
                        larmanController.modifyBundleProperties(selectedBundle);
                        yardPresenter.draw();
                        //elevationViewPresenter.setBundles(selectedBundle);
                        setFocusedBundleElevView(selectedBundle);
                    }
                    updateBundleInfo(selectedBundle);
                }
            }
        });

        bundleXPosValue.setOnKeyPressed(event -> {
            if (bundleXPosValue.isEditable()) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    if (selectedBundle != null) {
                        if (!bundleXPosValue.getText().isEmpty() && !bundleXPosValue.getText().equals("-") && !bundleXPosValue.getText().equals(".") && !bundleXPosValue.getText().equals("-.")) {
                            selectedBundle.position.setX(Double.parseDouble(bundleXPosValue.getText()));
                            larmanController.modifyBundlePosition(selectedBundle.id, selectedBundle.position);
                            yardPresenter.draw();
                            //elevationViewPresenter.setBundles(selectedBundle);
                            setFocusedBundleElevView(selectedBundle);
                        }
                        updateBundleInfo(selectedBundle);
                    }
                }
            }
        });

        bundleYPosValue.setOnKeyPressed(event -> {
            if (bundleYPosValue.isEditable()) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    if (selectedBundle != null) {
                        if (!bundleYPosValue.getText().isEmpty() && !bundleYPosValue.getText().equals("-") && !bundleYPosValue.getText().equals(".") && !bundleYPosValue.getText().equals("-.")) {
                            selectedBundle.position.setY(Double.parseDouble(bundleYPosValue.getText()));
                            larmanController.modifyBundlePosition(selectedBundle.id, selectedBundle.position);
                            //elevationViewPresenter.setBundles(selectedBundle);
                            setFocusedBundleElevView(selectedBundle);
                            yardPresenter.draw();
                        }
                        updateBundleInfo(selectedBundle);
                    }
                }
            }
        });

        bundleAngleValue.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if (selectedBundle != null) {
                    if (!bundleAngleValue.getText().isEmpty() && !bundleAngleValue.getText().equals("-") && !bundleAngleValue.getText().equals(".") && !bundleAngleValue.getText().equals("-.")) {
                        selectedBundle.angle = Double.parseDouble(bundleAngleValue.getText());
                        larmanController.modifyBundleProperties(selectedBundle);
                        yardPresenter.draw();
                        //elevationViewPresenter.setBundles(selectedBundle);
                        setFocusedBundleElevView(selectedBundle);
                    }
                    updateBundleInfo(selectedBundle);
                }
            }
        });
    }

    private void initInventorySearchBar() {
        inventorySearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            FilteredList<BundleDto> filteredData = new FilteredList<>(FXCollections.observableArrayList(observableBundleList));
            filteredData.setPredicate(bundleDto -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                if (bundleDto.getBarcode().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (bundleDto.getEssence().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (bundleDto.getPlankSize().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;

            });
            SortedList<BundleDto> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(inventoryTable.comparatorProperty());
            inventoryTable.setItems(sortedData);

        });
    }

    private void initTableView() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<BundleDto, String>("barcode"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<BundleDto, String>("essence"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<BundleDto, String>("plankSize"));
        inventoryTable.setRowFactory(tv -> {
            TableRow<BundleDto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    BundleDto bundle = row.getItem();
                    yardPresenter.setTopSelectedBundle(bundle);
                    clearElevationView();
                    //elevationViewPresenter.setBundles(bundle);
                    elevationViewPresenter3D.setFocusedBundle(bundle);
                    inventoryTable.getSelectionModel().select(bundle);
                    updateBundleInfo(bundle);
                } else {
                    yardPresenter.setTopSelectedBundle(null);
                    clearAllBundleInfo();
                    clearElevationView();
                }
            });
            return row;
        });
    }

    public YardPresenter getYardPresenter() {
        return yardPresenter;
    }

    public void clearAllBundleInfo() {
        this.selectedBundle = null;
        bundleBarcodeValue.clear();
        bundleLengthValue.clear();
        bundleWidthValue.clear();
        bundleHeightValue.clear();
        bundleDateValue.setValue(null);
        bundleHourValue.getValueFactory().setValue(0);
        bundleMinuteValue.getValueFactory().setValue(0);
        bundleEssenceValue.clear();
        bundlePlankSizeValue1.clear();
        bundlePlankSizeValue2.clear();
        bundleXPosValue.clear();
        bundleYPosValue.clear();
        bundleZPosValue.textProperty().setValue("");
        bundleAngleValue.clear();

    }


    public void updateLiftInfo(LiftDto liftDto){
        armsXValue.setText(String.valueOf(liftDto.armsPosition.getX()));
        armsYValue.setText(String.valueOf(liftDto.armsPosition.getY()));
        liftXValue.setText(String.valueOf(liftDto.position.getX()));
        liftYValue.setText(String.valueOf(liftDto.position.getY()));
        armsHeightValue.setText(String.valueOf(liftDto.armsHeight));
        liftAngleValue.setText(String.valueOf(liftDto.angle));

    }
    public void updateBundleInfo(BundleDto bundle) {
        this.selectedBundle = bundle;
        bundleBarcodeValue.setText(bundle.barcode);
        bundleLengthValue.setText(String.valueOf(bundle.length));
        bundleWidthValue.setText(String.valueOf(bundle.width));
        bundleHeightValue.setText(String.valueOf(bundle.height));
        bundleDateValue.setValue(bundle.date);
        bundleHourValue.getValueFactory().setValue(bundle.time.getHour());
        bundleMinuteValue.getValueFactory().setValue(bundle.time.getMinute());
        bundleEssenceValue.setText(bundle.essence);
        String[] plankSize = bundle.plankSize.split("x");
        bundlePlankSizeValue1.setText(plankSize[0]);
        bundlePlankSizeValue2.setText(plankSize[1]);
        bundleXPosValue.setText(String.valueOf(bundle.position.getX()));
        bundleYPosValue.setText(String.valueOf(bundle.position.getY()));
        bundleZPosValue.setText(String.valueOf(bundle.z));
        bundleAngleValue.setText(String.valueOf(bundle.angle));

        boolean canChange = true;
        for (BundleDto bundleDto : larmanController.getCollidingBundles(bundle)) {
            if (bundleDto.z > bundle.z) {
                canChange = false;
            }
        }
        /*bundleLengthValue.setEditable(canChange);
        bundleWidthValue.setEditable(canChange);
        bundleHeightValue.setEditable(canChange);
        bundleAngleValue.setEditable(canChange);*/
        bundleXPosValue.setEditable(canChange);
        bundleYPosValue.setEditable(canChange);
    }

    public void updateElevationView(BundleDto bundle) {
        //elevationViewPresenter.setBundles(bundle);
    }

    public void setFocusedBundleElevView(BundleDto bundle) {
        elevationViewPresenter3D.setFocusedBundle(bundle);
    }

    public void clearElevationView() {
        elevationViewPresenter3D.clearBundles();
    }

    public void clearTableView() {
        inventoryTable.getItems().clear();
    }

    public void addTableViewBundles(List<BundleDto> bundles) {
        inventorySearchBar.clear();
        if (!bundles.isEmpty()) {
            observableBundleList = bundles;
            ObservableList<BundleDto> data = FXCollections.observableArrayList(bundles);
            inventoryTable.setItems(data);
        } else {
            inventoryTable.getItems().clear();
        }
    }

    public void handleMenuFileNew(ActionEvent actionEvent) {
        FileHelper.newFile(stage, larmanController.getYard());
    }

    public void handleMenuFileOpen(ActionEvent actionEvent) {
        FileHelper.openFile(stage, larmanController.getYard());
    }

    public void handleMenuFileSave(ActionEvent actionEvent) {
        FileHelper.saveFile(stage, larmanController.getYard());
    }

    public void handleMenuSaveAs(ActionEvent actionEvent) {
        FileHelper.saveFileAs(stage, larmanController.getYard());
    }

    public void handleMenuHelpAbout(ActionEvent actionEvent) {
        JavafxHelper.popupView("About", "À propos", false, false);
    }

    public void handleExport3D() {
        if (!larmanController.getBundles().isEmpty()) {
            FileHelper.saveSTLFile(stage, larmanController.getBundles());
        }
    }

    public void handleUndoButton(ActionEvent actionEvent) {
        try {
            larmanController.setYard(UndoRedo.undo());
            yardPresenter.updateSelectedBundles();
            yardPresenter.draw();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleRedoButton(ActionEvent actionEvent) {
        try {
            larmanController.setYard(UndoRedo.redo());
            yardPresenter.updateSelectedBundles();
            yardPresenter.draw();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleGridSize(ActionEvent actionEvent) {
        JavafxHelper.popupGrid();
        yardPresenter.draw();
    }

}