import sys
import json
import math

from PySide2 import QtCore
from PySide2 import QtWidgets
from PySide2 import QtGui
from shiboken2 import wrapInstance

import maya.OpenMayaUI as omui
import maya.cmds as cmds

from maya.plugin.timeSliderBookmark.timeSliderBookmark import getAllBookmarks


FORMAT_VERSION = 4


def maya_main_window():
    main_window_ptr = omui.MQtUtil.mainWindow()
    if sys.version_info.major >= 3:
        return wrapInstance(int(main_window_ptr), QtWidgets.QWidget)
    else:
        return wrapInstance(long(main_window_ptr), QtWidgets.QWidget)





class AnimationModExportDialog(QtWidgets.QDialog):

    INFO_PIXMAP = QtGui.QPixmap(":info.png")

    def __init__(self, parent=maya_main_window()):
        super(AnimationModExportDialog, self).__init__(parent)
        self.setWindowTitle("Locomotion Animation Sequence Exporter")
        self.setMinimumWidth(500)

        # Remove the ? from the window
        self.setWindowFlag(QtCore.Qt.WindowContextHelpButtonHint, False)


        directory_a = "C:/Users/train/Desktop/Minecraft Modding/Animation Overhaul/src/main/resources/assets/locomotion/sequences"
        directory_b = "C:/Users/train/Desktop/Minecraft Modding/trainguys-animation-overhaul-1.19.x/src/main/resources/assets/locomotion/sequences"
        self.initial_directory = directory_b

        self.create_widgets()
        self.create_layouts()
        self.create_connections()

    def get_rgb_from_hex(self, code):
        code_hex = code.replace("#", "")
        rgb = tuple(int(code_hex[i:i+2], 16) for i in (0, 2, 4))
        return QtGui.QColor.fromRgb(rgb[0], rgb[1], rgb[2])

    def create_widgets(self):
        self.export_button = QtWidgets.QPushButton("Export")
        self.cancel_button = QtWidgets.QPushButton("Cancel")

        self.info_label_icon = QtWidgets.QLabel()
        self.info_label_icon.setPixmap(self.INFO_PIXMAP)
        self.info_label_text = QtWidgets.QLabel("JSON Format Version {0}".format(FORMAT_VERSION))
        self.info_label_text.setAlignment(QtCore.Qt.AlignCenter)

        self.directory_line_edit = QtWidgets.QLineEdit()
        self.directory_line_edit.setText(self.initial_directory)
        self.directory_line_edit_button = QtWidgets.QPushButton()
        self.directory_line_edit_button.setIcon(QtGui.QIcon(":fileOpen.png"))

        self.sets_combo_box = QtWidgets.QComboBox()
        self.sets_combo_box.setSizePolicy(QtWidgets.QSizePolicy.Expanding, QtWidgets.QSizePolicy.Expanding)
        self.update_combo_box()
        self.sets_combo_box_refresh_button = QtWidgets.QPushButton()
        self.sets_combo_box_refresh_button.setIcon(QtGui.QIcon(":refresh.png"))

        self.export_animation_curves_checkbox = QtWidgets.QCheckBox()
        self.export_animation_curves_checkbox.setChecked(True)

        self.export_time_markers_checkbox = QtWidgets.QCheckBox()
        self.export_time_markers_checkbox.setChecked(True)



        self.bookmark_list_layout = QtWidgets.QVBoxLayout()

        # Bookmark Selector Widgets

        self.info_conventions_label = QtWidgets.QLabel("Bookmark Naming:<br>Marker Naming:<br>Joint Set Naming:")
        self.info_conventions_text = QtWidgets.QLabel("SEQ_[path/to/animation]<br>MARKER_[identifier]<br>[rig_namespace]:exportSet_animation")

        #self.info_conventions_label.setFont(QtGui.QFont('times', 10))
        #self.info_conventions_text.setFont(QtGui.QFont('times', 10))

        self.animation_bookmark_refresh_button = QtWidgets.QPushButton("Refresh Bookmarks")
        self.animation_bookmark_refresh_button.setIcon(QtGui.QIcon(":refresh.png"))
        self.animation_bookmark_refresh_button.setToolTip("Refresh Animation Bookmarks")



    def create_layouts(self):
        info_label_layout = QtWidgets.QHBoxLayout()
        info_label_layout.addWidget(self.info_label_icon)
        info_label_layout.addWidget(self.info_label_text)
        info_label_layout.addStretch()

        directory_select_layout = QtWidgets.QHBoxLayout()
        directory_select_layout.addWidget(self.directory_line_edit)
        directory_select_layout.addWidget(self.directory_line_edit_button)

        sets_combo_box_layout = QtWidgets.QHBoxLayout()
        sets_combo_box_layout.addWidget(self.sets_combo_box)
        sets_combo_box_layout.addWidget(self.sets_combo_box_refresh_button)

        settings_form_layout = QtWidgets.QFormLayout()
        settings_form_layout.addRow("Resources Directory:", directory_select_layout)
        settings_form_layout.addRow("Export Set:", sets_combo_box_layout)
        settings_form_layout.addRow("Include Curves:", self.export_animation_curves_checkbox)
        settings_form_layout.addRow("Include Time Markers:", self.export_time_markers_checkbox)

        settings_layout = QtWidgets.QVBoxLayout()
        settings_layout.addLayout(settings_form_layout)
        settings_group_widget = QtWidgets.QGroupBox("Settings")
        settings_group_widget.setLayout(settings_layout)

        # Bookmark Selector Widgets

        bookmark_list_buttons = QtWidgets.QHBoxLayout()
        bookmark_list_buttons.addWidget(self.info_conventions_label)
        bookmark_list_buttons.addWidget(self.info_conventions_text)
        bookmark_list_buttons.addStretch()
        bookmark_list_buttons.addWidget(self.animation_bookmark_refresh_button)

        top_bookmark_divider = QtWidgets.QFrame()
        top_bookmark_divider.setFrameShape(QtWidgets.QFrame.HLine)
        top_bookmark_divider.setFrameShadow(QtWidgets.QFrame.Sunken)

        self.update_bookmarks_list()

        bookmarks_layout = QtWidgets.QVBoxLayout()
        bookmarks_layout.addLayout(bookmark_list_buttons)
        bookmarks_layout.addWidget(top_bookmark_divider)
        bookmarks_layout.addLayout(self.bookmark_list_layout)
        bookmarks_group_widget = QtWidgets.QGroupBox("Animation Sequences")
        bookmarks_group_widget.setLayout(bookmarks_layout)

        button_layout = QtWidgets.QHBoxLayout()
        button_layout.addStretch()
        button_layout.addWidget(self.export_button)
        button_layout.addWidget(self.cancel_button)

        main_layout = QtWidgets.QVBoxLayout(self)
        main_layout.addLayout(info_label_layout)
        main_layout.addWidget(settings_group_widget)
        main_layout.addWidget(bookmarks_group_widget)
        main_layout.addStretch()
        main_layout.addLayout(button_layout)

    def create_connections(self):
        self.animation_bookmark_refresh_button.clicked.connect(self.update_bookmarks_list)
        self.export_button.pressed.connect(self.export)
        self.sets_combo_box_refresh_button.clicked.connect(self.update_combo_box)
        self.directory_line_edit_button.clicked.connect(self.show_folder_select)
        pass

    def show_folder_select(self):
        new_directory = QtWidgets.QFileDialog.getExistingDirectory(self, "Select Folder", self.initial_directory)
        if new_directory:
            self.directory_line_edit.setText(new_directory)
            self.initial_directory = new_directory

    def update_combo_box(self):
        sets = cmds.ls(type="objectSet")
        self.sets_combo_box.clear()
        self.export_button.setEnabled(False)
        self.export_button.setToolTip("Cannot export without a valid export set.")
        for set in sets:
            if "exportSet_animation" in set:
                self.sets_combo_box.addItem(set)
                self.export_button.setEnabled(True)
                self.export_button.setToolTip("Export animation")

    def clear_layout(self, layout):
        if layout is not None:
            while layout.count():
                item = layout.takeAt(0)
                widget = item.widget()
                if widget is not None:
                    widget.deleteLater()
                else:
                    self.clear_layout(item.layout())

    def update_bookmarks_list(self):

        self.bookmarks = []
        self.sequenceNames = []
        self.markers = {}

        data = []
        bookmarks = getAllBookmarks()
        for bookmark in bookmarks:
            if cmds.getAttr("{}.name".format(bookmark)).split("_")[0] == "SEQ":
                color = cmds.getAttr("{}.color".format(bookmark))[0]
                name = '_'.join(cmds.getAttr("{}.name".format(bookmark)).split("_")[1:])
                self.markers[bookmark] = []

                markers = []
                for markerBookmark in bookmarks:
                    if cmds.getAttr(f"{markerBookmark}.name").split("_")[0] == "MARKER":
                        markerBeforeEndOfSequence = cmds.getAttr(f"{markerBookmark}.timeRangeStart") <= cmds.getAttr(f"{bookmark}.timeRangeStop")
                        markerAfterBeginningOfSequence = cmds.getAttr(f"{markerBookmark}.timeRangeStart") >= cmds.getAttr(f"{bookmark}.timeRangeStart")
                        if markerBeforeEndOfSequence and markerAfterBeginningOfSequence:
                            markers.append(markerBookmark)
                            self.markers[bookmark].append(markerBookmark)

                data.append([
                    name,
                    '#%02x%02x%02x' % (int(color[0] * 255), int(color[1] * 255), int(color[2] * 255)),
                    str(int(cmds.getAttr(f"{bookmark}.timeRangeStart"))).zfill(4),
                    str(int(cmds.getAttr(f"{bookmark}.timeRangeStop"))).zfill(4),
                    markers
                ])
                self.bookmarks.append(bookmark)
                self.sequenceNames.append(name)



        self.clear_layout(self.bookmark_list_layout)

        tempLayout = QtWidgets.QVBoxLayout()
        self.bookmark_checkbox_widgets = []

        for i, (name, code, start, end, markers) in enumerate(data):
            column_layout = QtWidgets.QVBoxLayout()
            row_layout = QtWidgets.QHBoxLayout()

            bookmark_label_widget = QtWidgets.QLabel(name)
            bookmark_checkbox_widget = QtWidgets.QCheckBox()
            bookmark_color_widget = QtWidgets.QPushButton()
            bookmark_range_widget = QtWidgets.QLabel("{} - {}".format(start, end))

            self.bookmark_checkbox_widgets.append(bookmark_checkbox_widget)

            bookmark_color_widget.setFixedWidth(24)
            bookmark_color_widget.setEnabled(False)
            bookmark_color_widget.setStyleSheet("background-color : {}".format(code))
            #bookmark_color_widget.set(self.get_rgb_from_hex(code))

            bookmark_checkbox_widget.setChecked(True)

            row_layout.addWidget(bookmark_label_widget)
            row_layout.addStretch()
            row_layout.addWidget(bookmark_checkbox_widget)
            row_layout.addWidget(bookmark_range_widget)
            row_layout.addWidget(bookmark_color_widget)

            column_layout.addLayout(row_layout)

            for marker in markers:

                marker_data_layout = QtWidgets.QHBoxLayout()
                marker_label_widget = QtWidgets.QLabel('_'.join(cmds.getAttr(f"{marker}.name").split('_')[1:]))
                marker_label_widget.setContentsMargins(20, 0, 0, 0)
                marker_time_widget = QtWidgets.QLabel(str(int(cmds.getAttr(f"{marker}.timeRangeStart"))).zfill(4))

                marker_data_layout.addWidget(marker_label_widget)
                marker_data_layout.addStretch()
                marker_data_layout.addWidget(marker_time_widget)
                column_layout.addLayout(marker_data_layout)


            tempLayout.addLayout(column_layout)
        self.bookmark_list_layout.addLayout(tempLayout)

    def get_frame_rate(self):
        #return 20
        value = str(cmds.currentUnit(q=True, time=True))
        if "film" in value:
            return 24
        if "fps" in value:
            return int(value.split('fps')[0])
        if "ntscf" in value:
            return 60
        if "ntsc" in value:
            return 30
        return 0

    def export(self):
        directory = self.directory_line_edit.text()
        exportSet = self.sets_combo_box.currentText()

        numberOfJointsInSet = cmds.sets(exportSet, size=True, query=True)
        numberOfBookmarks = len(self.bookmarks)

        frameRate = self.get_frame_rate()
        exportedSequences = []
        for i in range(numberOfBookmarks):

            sequencePathAndName = self.sequenceNames[i]
            filePath = f"{directory}/{sequencePathAndName}.json"
            timelineBookmark = self.bookmarks[i]
            markedForExport = self.bookmark_checkbox_widgets[i].isChecked()

            actualDirectory = '/'.join(filePath.split('/')[:-1])
            if not os.path.isdir(actualDirectory):
                QtWidgets.QMessageBox.critical(self, "Export Failure", f"The directory {actualDirectory} does not exist.")
                return
                print(actualDirectory)

            if markedForExport:
                self.export_sequence(timelineBookmark, filePath, exportSet, frameRate)
                print(timelineBookmark)
                print(filePath)
                print(exportSet)
                print(frameRate)
                exportedSequences.append(sequencePathAndName.split('/')[-1])

        QtWidgets.QMessageBox.information(self, "Export Complete", f"Successfully exported the following animation sequences: \n\n{', '.join(exportedSequences)}")
        pass

    def export_sequence(self, timelineBookmark, filePath, exportSet, frameRate):
        if frameRate == 0:
            QtWidgets.QMessageBox.critical(self, "Export Failure", "The scene's frame rate is invalid.")
            return

        frameRateConversionMultiplier = 1 / frameRate
        startTime = round(cmds.getAttr(f"{timelineBookmark}.timeRangeStart"))
        endTime = round(cmds.getAttr(f"{timelineBookmark}.timeRangeStop")) + 1
        length = round((endTime - startTime) * frameRateConversionMultiplier,3)

        joints = cmds.sets(exportSet, query=True)
        joints.sort()

        jsonDict = {
            "format_version": FORMAT_VERSION,
            "length": length
        }

        jsonDict["joints"] = {}
        jsonDict["time_markers"] = {}
        jsonDict["curves"] = {}

        if self.export_time_markers_checkbox.isChecked():
            for marker in self.markers[timelineBookmark]:
                markerName = '_'.join(cmds.getAttr(f"{marker}.name").split('_')[1:])
                if markerName not in jsonDict["time_markers"].keys():
                    jsonDict["time_markers"][markerName] = []
                markerTimeConverted = round((cmds.getAttr(f"{marker}.timeRangeStart") - startTime) * frameRateConversionMultiplier, 3)
                jsonDict["time_markers"][markerName].append(markerTimeConverted)


        for joint in joints:
            jointWithoutNamespace = joint.split(':')[-1]
            jsonDict["joints"][jointWithoutNamespace] = {}
            jsonDict["joints"][jointWithoutNamespace]["translation"] = {}
            jsonDict["joints"][jointWithoutNamespace]["rotation"] = {}
            jsonDict["joints"][jointWithoutNamespace]["scale"] = {}
            jsonDict["joints"][jointWithoutNamespace]["visibility"] = {}

            previousTranslation = []
            previousRotation = []
            previousScale = []
            previousVisibility = None
            previousConvertedTime = 0

            for time in range(startTime, endTime + 1):
                cmds.currentTime(time)
                convertedTime = round((time - startTime) * frameRateConversionMultiplier, 3)

                translateX = round(cmds.getAttr(f"{joint}.translateX"), 3)
                translateY = -round(cmds.getAttr(f"{joint}.translateY"), 3)
                translateZ = -round(cmds.getAttr(f"{joint}.translateZ"), 3)
                translation = [translateX, translateY, translateZ]

                rotateX = round(cmds.getAttr(f"{joint}.rotateX"), 3)
                rotateY = -round(cmds.getAttr(f"{joint}.rotateY"), 3)
                rotateZ = -round(cmds.getAttr(f"{joint}.rotateZ"), 3)
                rotation = [rotateX, rotateY, rotateZ]

                scaleX = round(cmds.getAttr(f"{joint}.scaleX"), 3)
                scaleY = round(cmds.getAttr(f"{joint}.scaleY"), 3)
                scaleZ = round(cmds.getAttr(f"{joint}.scaleZ"), 3)
                scale = [scaleX, scaleY, scaleZ]

                visibility = True
                hasVisibilityAttribute = cmds.attributeQuery('elementVisibility', node=joint, exists=True)
                if hasVisibilityAttribute:
                    visibilityAttributeIsOfCorrectType = cmds.attributeQuery('elementVisibility', node=joint, attributeType=True) == 'bool'
                    if visibilityAttributeIsOfCorrectType:
                        visibility = cmds.getAttr(f"{joint}.elementVisibility")

                if previousVisibility == None:
                    jsonDict["joints"][jointWithoutNamespace]["visibility"][convertedTime] = visibility
                    previousVisibility = visibility
                if visibility != previousVisibility:
                    jsonDict["joints"][jointWithoutNamespace]["visibility"][previousConvertedTime] = previousVisibility
                    jsonDict["joints"][jointWithoutNamespace]["visibility"][convertedTime] = visibility
                    previousVisibility = visibility

                if previousTranslation == []:
                    jsonDict["joints"][jointWithoutNamespace]["translation"][convertedTime] = translation
                    previousTranslation = translation
                if translation != previousTranslation:
                    jsonDict["joints"][jointWithoutNamespace]["translation"][previousConvertedTime] = previousTranslation
                    jsonDict["joints"][jointWithoutNamespace]["translation"][convertedTime] = translation
                    previousTranslation = translation

                if previousRotation == []:
                    jsonDict["joints"][jointWithoutNamespace]["rotation"][convertedTime] = rotation
                    previousRotation = rotation
                if rotation != previousRotation:
                    jsonDict["joints"][jointWithoutNamespace]["rotation"][previousConvertedTime] = previousRotation
                    jsonDict["joints"][jointWithoutNamespace]["rotation"][convertedTime] = rotation
                    previousRotation = rotation

                if previousScale == []:
                    jsonDict["joints"][jointWithoutNamespace]["scale"][convertedTime] = scale
                    previousScale = scale
                if scale != previousScale:
                    jsonDict["joints"][jointWithoutNamespace]["scale"][previousConvertedTime] = previousScale
                    jsonDict["joints"][jointWithoutNamespace]["scale"][convertedTime] = scale
                    previousScale = scale

                #jsonDict["joints"][joint]["translation"][convertedTime] = translation
                previousConvertedTime = convertedTime
                pass






        debug = False
        if debug:
            print(json.dumps(jsonDict, indent=4))
        else:
            with open(filePath, 'w') as outfile:
                json.dump(jsonDict, outfile, indent=4)
        pass


if __name__ == "__main__":

    try:
        animation_mod_export_dialog.close() # pylint: disable=E0601
        animation_mod_export_dialog.deleteLater()
    except:
        pass

    animation_mod_export_dialog = AnimationModExportDialog()
    animation_mod_export_dialog.show()


"""
locomotion_directory = 
sequences_path = "C:/Users/train/Desktop/Minecraft Modding/trainguys-animation-overhaul-1.19.x/src/main/resources/assets/locomotion/sequences"
script_path = "C:/Users/train/Desktop/Minecraft Modding/trainguys-animation-overhaul-1.19.x/src/main/python"
"""