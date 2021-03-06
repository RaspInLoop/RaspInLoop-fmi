/*******************************************************************************
 * Copyright (C) 2018 RaspInLoop
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.raspinloop.pi4j.io.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.raspinloop.config.HardwareProperties;
import org.raspinloop.config.Pin;
import org.raspinloop.config.PinState;
import org.raspinloop.fmi.plugin.Images;
import org.raspinloop.fmi.plugin.preferences.extension.AbstractHWConfigPage;
import org.raspinloop.pi4j.io.NaturalOrderComparator;


public class SimulatedStepperMotorPropertiesPage extends AbstractHWConfigPage {


	public class PinLabelProvider extends LabelProvider{
		@Override
		public String getText(Object element) {
			if (element instanceof Pin)
				return ((Pin)element).getName();
			else return super.getText(element);
		}
	}

	public class AvaillablePinProvider implements IStructuredContentProvider {

		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Collection<?>){
				
				 ArrayList<Pin> arrayList = new ArrayList<Pin>((Collection<? extends Pin>)inputElement);
				//Sorting
				final NaturalOrderComparator comparator = new NaturalOrderComparator();
				Collections.sort(arrayList, new Comparator<Pin>() {
				        @Override
				        public int compare(Pin  pin1, Pin  pin2)
				        {
				        	return comparator.compare(pin1.getName(), pin2.getName());
				        }
				    });
				
				return arrayList.toArray();
			}
			return null;
		}
	}

	private IStatus[] fFieldStatus = new IStatus[1];
	private Text fHWName;
	private SimulatedStepperMotorProperties fHW;
	private Spinner fHWStepsPerRotation;
	private Spinner fHWInitialPosition;
	private Combo fHWOnState;
	private ComboViewer [] fHWPins = new ComboViewer [4];
	private Spinner fHWHoldingTorque;
	@SuppressWarnings("unused")
	private Spinner fHWRotorIntertia;

	public SimulatedStepperMotorPropertiesPage() {
		super("Simulated StepperMotor configuration");
		for (int i = 0; i < fFieldStatus.length; i++) {
			fFieldStatus[i] = Status.OK_STATUS;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite p) {
		// create a composite with standard margins and spacing
		Composite composite = new Composite(p, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		addLabel(composite, "Stepper Motor Name");
		fHWName = addText(composite);

		addLabel(composite, "Step by Rotation");

		fHWStepsPerRotation = new Spinner(composite, SWT.NONE);
		fHWStepsPerRotation.setDigits(0);
		fHWStepsPerRotation.setMinimum(0);
		fHWStepsPerRotation.setMaximum(400);
		fHWStepsPerRotation.setIncrement(1);
		fHWStepsPerRotation.setSelection(200);

		addLabel(composite, "Initial Position (�)");
		fHWInitialPosition = new Spinner(composite, SWT.NONE);
		fHWInitialPosition.setDigits(1);
		fHWInitialPosition.setMinimum(0);
		fHWInitialPosition.setMaximum(3600);
		fHWInitialPosition.setIncrement(1);
		fHWInitialPosition.setSelection(72);

		addLabel(composite, "Holding Torque (N.m)");
		fHWHoldingTorque = new Spinner(composite, SWT.NONE);
		fHWHoldingTorque.setDigits(2);
		fHWHoldingTorque.setMinimum(0);
		fHWHoldingTorque.setMaximum(10000);
		fHWHoldingTorque.setIncrement(1);
		fHWHoldingTorque.setSelection(44);
					
		addLabel(composite, "ON state");
		fHWOnState = new Combo(composite, SWT.READ_ONLY);
		fHWOnState.setItems(new String[] { PinState.HIGH.toString(), PinState.LOW.toString() });

		addLabel(composite, "Connected Pin");
		ArrayList<Pin> usablePins = new ArrayList<Pin>(fHW.getParentComponent().getUnUsedPins());
		usablePins.addAll(fHW.getUsedPins());
		
		Composite pinBlockComposite = new Composite(composite, SWT.NONE);
		GridLayout pinBlockLayout = new GridLayout();
		pinBlockLayout.numColumns = 4;
		pinBlockLayout.marginLeft=0;
		pinBlockComposite.setLayout(pinBlockLayout);
		pinBlockComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		AvaillablePinProvider availablePinProvider = new AvaillablePinProvider();
		PinLabelProvider pinLableProvider = new PinLabelProvider();
		
		for (int i = 0; i < fHWPins.length; i++) {
			ComboViewer  hWPin = new ComboViewer (pinBlockComposite, SWT.READ_ONLY);
			hWPin.setContentProvider(availablePinProvider);
			hWPin.setLabelProvider(pinLableProvider);	
			
			hWPin.setInput(usablePins);
			fHWPins[i] = hWPin;
		}
		
		// add the listeners now to prevent them from monkeying with initialized
		// settings
		fHWName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				nameChanged(fHWName.getText());
			}
		});

		
		
		Dialog.applyDialogFont(composite);
		setControl(composite);
		initializeFields();
	}

	/**
	 * Initialize the dialogs fields
	 */
	private void initializeFields() {
		fHWName.setText(fHW.getComponentName());
		Pin[] pins = fHW.getUsedPins().toArray(new Pin[0]);
		for (int j = 0; j < pins.length; j++) {
			fHWPins[j].setSelection(new StructuredSelection(pins[j]));
		}

		fHWStepsPerRotation.setSelection(fHW.getStepsPerRotation());
		fHWInitialPosition.setSelection((int) Math.round(fHW.getInitalPosition() * 10));
		fHWOnState.setText(fHW.getOnState().toString());
		fHWHoldingTorque.setSelection((int) Math.round(fHW.getHoldingTorque()*100));
		nameChanged(fHWName.getText());
	}

	private void setFieldValuesToHW() {
		fHW.setComponentName(fHWName.getText());
		ArrayList<Pin> pins2 = new ArrayList<>(4);
		for (ComboViewer comboPin : fHWPins) {
			IStructuredSelection  selection = (IStructuredSelection) comboPin.getSelection();
			Pin pin = (Pin) selection.getFirstElement();
			if (pin != null)				
				pins2.add(pin);	
		}
		fHW.setPins(pins2);			

		fHW.setInitalPosition(fHWInitialPosition.getSelection() / 10.0);
		fHW.setHoldingTorque(fHWHoldingTorque.getSelection() / 10.0);
		fHW.setOnState(PinState.valueOf(fHWOnState.getText()));
		if (fHW.getOnState().equals(PinState.HIGH))
			fHW.setOffState(PinState.LOW);
		else
			fHW.setOffState(PinState.HIGH);
		fHW.setStepsPerRotation(fHWStepsPerRotation.getSelection());
	}	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	@Override
	public Image getImage() {
		return Images.get(Images.IMG_HARDWARE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage#
	 * getVMStatus()
	 */
	@Override
	protected IStatus[] getHWStatus() {
		return fFieldStatus;
	}

	@Override
	public boolean finish() {
		setFieldValuesToHW();
		return true;
	}
	
	@Override
	public void setSelection(HardwareProperties hw) {
		super.setSelection(hw);
		if (hw instanceof SimulatedStepperMotorProperties)
			fHW = (SimulatedStepperMotorProperties)hw;		
		setTitle("Configure Stepper Motor");
		setDescription("Use this page to configure your Simulated Stepper Motor.");
	}

	@Override
	public HardwareProperties getSelection() {
		return fHW;
	}
}
