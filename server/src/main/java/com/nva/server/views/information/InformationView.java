package com.nva.server.views.information;

import com.nva.server.constants.CustomConstants;
import com.nva.server.entities.Action;
import com.nva.server.entities.Information;
import com.nva.server.entities.Scope;
import com.nva.server.entities.Topic;
import com.nva.server.services.ActionService;
import com.nva.server.services.InformationService;
import com.nva.server.services.ScopeService;
import com.nva.server.services.TopicService;
import com.nva.server.utils.CustomUtils;
import com.nva.server.views.MainLayout;
import com.nva.server.views.components.CustomNotification;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.model.Cursor;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessDeniedErrorRouter;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Sort;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PageTitle("Information | Management")
@Route(value = "admin/information", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMIN")
@AccessDeniedErrorRouter
public class InformationView extends VerticalLayout {
    private final InformationService informationService;
    private final ActionService actionService;
    private final ScopeService scopeService;
    private final TopicService topicService;

    private final Grid<Information> informationGrid = new Grid<>(Information.class);
    private final TextField filterText = new TextField();
    private final Dialog editInformationDialog = new Dialog();
    private final Dialog createNewInformationDialog = new Dialog();
    private final Dialog confirmDeleteInformationDialog = new Dialog();
    private InformationForm editInformationForm;
    private InformationForm createNewInformationForm;

    // Additional filter variables
    private final Dialog additionalFilterDialog = new Dialog();
    private final ComboBox<Action> actionComboBox = new ComboBox<>();
    private final ComboBox<Scope> scopeComboBox = new ComboBox<>();
    private final ComboBox<Topic> topicComboBox = new ComboBox<>();

    // Pagination variables
    private int pageNumber = 0;
    private final Button prevPageButton = new Button(new Icon(VaadinIcon.ANGLE_LEFT));
    private final Button nextPageButton = new Button(new Icon(VaadinIcon.ANGLE_RIGHT));
    private final Paragraph paginationLabel = new Paragraph();
    private Map<String, Object> params = new HashMap<>();

    public InformationView(InformationService informationService, ActionService actionService, ScopeService scopeService, TopicService topicService) {
        this.actionService = actionService;
        this.scopeService = scopeService;
        this.topicService = topicService;
        this.informationService = informationService;

        addClassName("information-view");
        setSizeFull();
        configureGrid();
        configureForms();
        configureDialogs();

        add(getToolbar(), getContent());

        updateInformationList();
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(informationGrid);
        content.addClassName("content");
        content.setSizeFull();

        return content;
    }

    private void configureDialogs() {
        configureEditInformationDialog();
        configureCreateNewInformationDialog();
        configureDeleteInformationDialog();
        configureAdditionalFilterDialog();
    }

    private void configureAdditionalFilterDialog() {
        actionComboBox.setPlaceholder("Choose action");
        actionComboBox.setWidthFull();
        actionComboBox.setItems(actionService.getActions(new HashMap<>()));
        actionComboBox.setItemLabelGenerator(a -> String.format("%s - %s", a.getName(), a.getDescription()));

        scopeComboBox.setPlaceholder("Choose scope");
        scopeComboBox.setWidthFull();
        scopeComboBox.setItems(scopeService.getScopes(new HashMap<>()));
        scopeComboBox.setItemLabelGenerator(s -> String.format("%s - %s", s.getName(), s.getDescription()));

        topicComboBox.setPlaceholder("Choose topic");
        topicComboBox.setWidthFull();
        topicComboBox.setItems(topicService.getAllTopics(Sort.by(Sort.Direction.ASC, "name")));
        topicComboBox.setItemLabelGenerator(t -> String.format("%s - %s", t.getName(), t.getDescription()));

        VerticalLayout filterGroupLayout = new VerticalLayout(actionComboBox, scopeComboBox, topicComboBox);
        filterGroupLayout.setWidth("25em");
        filterGroupLayout.setPadding(false);

        additionalFilterDialog.setHeaderTitle("Additional Filter");
        additionalFilterDialog.add(filterGroupLayout);
        Button applyButton = new Button("Apply", e -> applyInformationFilter());
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyButton.getStyle().setCursor(Cursor.POINTER.name());

        Button cancelButton = new Button("Cancel", e -> closeAdditionalFilterDialog());
        cancelButton.getStyle().setCursor(Cursor.POINTER.name());

        additionalFilterDialog.getFooter().add(applyButton, cancelButton);
    }

    private void applyInformationFilter() {
        filterText.setValue("");

        params = new HashMap<>();

        if (actionComboBox.getValue() != null) {
            params.put("actionId", actionComboBox.getValue().getId());
        }
        if (scopeComboBox.getValue() != null) {
            params.put("scopeId", scopeComboBox.getValue().getId());
        }
        if (topicComboBox.getValue() != null) {
            params.put("topicId", topicComboBox.getValue().getId());
        }

        updateInformationList();
        additionalFilterDialog.close();
    }

    private void configureDeleteInformationDialog() {
        confirmDeleteInformationDialog.setHeaderTitle("Delete information");
        confirmDeleteInformationDialog.add(new Paragraph("Are you sure you want to delete this information?"));
        Paragraph subtitle = new Paragraph("It cannot be restored after deleting.");
        subtitle.getStyle().setFontWeight(800);
        confirmDeleteInformationDialog.add(subtitle);

        Button deleteButton = new Button("Delete", e -> {
            deleteInformation(editInformationForm.getInformation());
            closeConfirmDeleteInformationDialog();
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteButton.getStyle().setCursor("pointer");

        Button cancelButton = new Button("Cancel", e -> closeConfirmDeleteInformationDialog());
        cancelButton.getStyle().setCursor("pointer");

        confirmDeleteInformationDialog.getFooter().add(deleteButton, cancelButton);

        add(confirmDeleteInformationDialog);
    }

    private void configureCreateNewInformationDialog() {
        createNewInformationDialog.setHeaderTitle("Add new");
        createNewInformationDialog.getFooter().add(createNewInformationForm.getSaveButton(), createNewInformationForm.getCancelButton());

        add(createNewInformationDialog);
    }

    private void configureEditInformationDialog() {
        editInformationDialog.setHeaderTitle("Edit information");
        editInformationForm.getDeleteButton().setVisible(false);
        editInformationDialog.getFooter().add(editInformationForm.getSaveButton(), editInformationForm.getCancelButton());

        add(editInformationDialog);
    }

    private void configureForms() {
        editInformationForm = new InformationForm(actionService, scopeService, topicService);
        editInformationForm.setWidth("25em");
        editInformationForm.setVisible(true);
        editInformationForm.addListener(InformationForm.SaveEvent.class, e -> editInformation(e.getInformation()));
        editInformationForm.addListener(InformationForm.DeleteEvent.class, e -> deleteInformation(e.getInformation()));
        editInformationForm.addListener(InformationForm.CloseEvent.class, e -> closeEditInformationDialog());

        createNewInformationForm = new InformationForm(actionService, scopeService, topicService);
        createNewInformationForm.setWidth("25em");
        createNewInformationForm.setVisible(true);
        createNewInformationForm.getDeleteButton().setVisible(false);
        createNewInformationForm.getShowedCreatedDate().setVisible(false);
        createNewInformationForm.getShowedLastModifiedDate().setVisible(false);
        createNewInformationForm.addListener(InformationForm.SaveEvent.class, e -> saveInformation(e.getInformation()));
        createNewInformationForm.addListener(InformationForm.CloseEvent.class, e -> closeCreateNewInformationDialog());
    }

    private void closeCreateNewInformationDialog() {
        createNewInformationDialog.close();
        createNewInformationForm.setInformation(null);
        removeClassName("information-creating");
    }

    private void saveInformation(Information action) {
        try {
            informationService.saveInformation(action);
            updateInformationList();
            CustomNotification.showNotification("Created successfully!", "success", Notification.Position.TOP_CENTER, 3000);

            closeCreateNewInformationDialog();
        } catch (Exception ex) {
            CustomNotification.showNotification(ex.getMessage(), "error", Notification.Position.TOP_CENTER, 3000);
        }
    }

    private void deleteInformation(Information action) {
        try {
            informationService.removeInformation(action);
            updateInformationList();
            CustomNotification.showNotification("Deleted successfully!", "success", Notification.Position.TOP_CENTER, 3000);

            closeEditInformationDialog();
        } catch (Exception ex) {
            CustomNotification.showNotification(ex.getMessage(), "error", Notification.Position.TOP_CENTER, 3000);
        }
    }

    private void editInformation(Information action) {
        try {
            informationService.editInformation(action);
            updateInformationList();
            CustomNotification.showNotification("Updated successfully!", "success", Notification.Position.TOP_CENTER, 3000);

            closeEditInformationDialog();
        } catch (Exception ex) {
            CustomNotification.showNotification(ex.getMessage(), "error", Notification.Position.TOP_CENTER, 3000);
        }
    }

    private void configureGrid() {
        informationGrid.addClassName("information-grid");
        informationGrid.setSizeFull();
        informationGrid.setColumns("content");
        informationGrid.addColumn(information -> String.format("%s.%s.%s", information.getAction().getName(), information.getScope().getName(), information.getTopic().getName())).setHeader("Intent");
        informationGrid.addColumn(information -> CustomUtils.convertMillisecondsToDate(information.getCreatedDate(), "HH:mm:ss dd-MM-yyyy")).setHeader("Created date");
        informationGrid.addColumn(information -> (information.getLastModifiedDate() == null) ? "--" :
                CustomUtils.convertMillisecondsToDate(information.getLastModifiedDate(), "HH:mm:ss dd-MM-yyyy")).setHeader("Last modified date");
        informationGrid.addColumn(information -> (information.getNote() == null || information.getNote().isEmpty()) ? "--" : information.getNote()).setHeader("Note");
        informationGrid.addColumn(new ComponentRenderer<>(MenuBar::new, this::configureMenuBar)).setHeader("Actions");

        informationGrid.getColumns().forEach(col -> col.setWidth("200px"));
    }

    private void configureMenuBar(MenuBar menuBar, Information information) {
        menuBar.addItem("Edit", e -> openEditInformationDialog(information)).getStyle().setCursor("pointer");
        menuBar.addItem("Delete", e -> openConfirmDeleteInformationDialog(information)).getStyle().setCursor("pointer").setColor("red");
    }

    private void openConfirmDeleteInformationDialog(Information information) {
        editInformationForm.setInformation(information);
        confirmDeleteInformationDialog.open();
    }

    private void closeConfirmDeleteInformationDialog() {
        confirmDeleteInformationDialog.close();
    }

    private void openEditInformationDialog(Information information) {
        if (information == null) closeEditInformationDialog();
        else {
            editInformationForm.setInformation(information);
            editInformationDialog.add(new HorizontalLayout(editInformationForm));
            editInformationDialog.open();
            addClassName("information-editing");
        }
    }

    private void closeEditInformationDialog() {
        editInformationDialog.close();
        editInformationForm.setInformation(null);
        removeClassName("information-editing");
    }

    private HorizontalLayout getToolbar() {

        filterText.setPlaceholder("Filter by content...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY); // LAZY: wait user stop typing --> do filter
        filterText.addValueChangeListener(e -> updateInformationList());

        Button additionalFilterButton = new Button(LineAwesomeIcon.FILTER_SOLID.create());
        additionalFilterButton.getStyle().setCursor(Cursor.POINTER.name());
        additionalFilterButton.addClickListener(e -> openAdditionalFilterDialog());

        Button addInformationButton = new Button("Add new");
        addInformationButton.getStyle().setCursor("pointer");
        addInformationButton.addClickListener(e -> openCreateNewInformationDialog());

        prevPageButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        prevPageButton.getStyle().setCursor(Cursor.POINTER.name());
        prevPageButton.setTooltipText("Previous page");
        prevPageButton.addClickListener(e -> {
            if (pageNumber > 0) {
                pageNumber--;
                updateInformationList();
            }
        });

        nextPageButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        nextPageButton.getStyle().setCursor(Cursor.POINTER.name());
        nextPageButton.setTooltipText("Next page");
        nextPageButton.addClickListener(e -> {
            long totalMajorsCount = (int) informationService.getInformationCount(new HashMap<>()); // Implement this method in your service
            int totalPages = (int) Math.ceil((double) totalMajorsCount / CustomConstants.INFORMATION_PAGE_SIZE);
            if (pageNumber < totalPages - 1) {
                pageNumber++;
                updateInformationList();
            }
        });

        HorizontalLayout filterLayout = new HorizontalLayout(additionalFilterButton, filterText, addInformationButton);

        HorizontalLayout paginationController = new HorizontalLayout(paginationLabel, prevPageButton, nextPageButton);

        HorizontalLayout toolbar = new HorizontalLayout(filterLayout, paginationController);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbar.addClassName("toolbar");

        return toolbar;
    }

    private void openAdditionalFilterDialog() {
        additionalFilterDialog.open();
    }

    private void closeAdditionalFilterDialog() {
        additionalFilterDialog.close();

        actionComboBox.setValue(null);
        scopeComboBox.setValue(null);
        topicComboBox.setValue(null);
    }

    private void openCreateNewInformationDialog() {
        createNewInformationDialog.add(new HorizontalLayout(createNewInformationForm));
        createNewInformationDialog.open();
        createNewInformationForm.setInformation(new Information());
        addClassName("information-creating");
    }

    private void updateInformationList() {
        String keyword = filterText.getValue();
        params.put("pageNumber", pageNumber);
        params.put("pageSize", CustomConstants.INFORMATION_PAGE_SIZE);
        params.put("keyword", keyword);

        List<Information> information = informationService.getInformation(params);
        long totalInformation = informationService.getInformationCount(params);

        informationGrid.setItems(information);

        updatePaginationButtons();
        updatePaginationLabel(totalInformation);
    }

    private void updatePaginationLabel(long totalInformation) {
        int startInformation = pageNumber * CustomConstants.INFORMATION_PAGE_SIZE + 1;
        int endInformation = (int) Math.min((long) (pageNumber + 1) * CustomConstants.INFORMATION_PAGE_SIZE, totalInformation);

        // Update label with the current range of users being displayed
        paginationLabel.setText(startInformation + " - " + endInformation + " out of " + totalInformation);
    }

    private void updatePaginationButtons() {
        long totalInformationCount = informationService.getInformationCount(new HashMap<>());
        int totalPages = (int) Math.ceil((double) totalInformationCount / CustomConstants.INFORMATION_PAGE_SIZE);

        prevPageButton.setEnabled(pageNumber > 0);
        nextPageButton.setEnabled(pageNumber < totalPages - 1);
    }
}
