/*******************************************************************************
 * Copyright (C) 2018 RaspInLoop
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.raspinloop.fmi.plugin.preferences;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.dialogs.SearchPattern;
import org.raspinloop.config.HardwareProperties;
import org.raspinloop.fmi.plugin.Activator;

public class HardwareSelectionDialog extends FilteredItemsSelectionDialog {

	public class HwLabelProvider implements ILabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {}

		@Override
		public void dispose() {			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) { return false;}

		@Override
		public void removeListener(ILabelProviderListener listener) {}

		@Override
		public Image getImage(Object element) {			
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof HardwareProperties ){
				HardwareProperties hwConfig = (HardwareProperties) element;
				return hwConfig.getComponentName();
			}
			return element.toString();
		}		
	}
	
	public class HwDetailLabelProvider implements ILabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {}

		@Override
		public void dispose() {			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) { return false;}

		@Override
		public void removeListener(ILabelProviderListener listener) {}

		@Override
		public Image getImage(Object element) {			
			return null;
		}

		@Override
		public String getText(Object element) {			
			return element.getClass().getName();
		}		
	}
	public class HwSearchRequestor {

		private AbstractContentProvider contentProvider;
		private HardwareItemsFilter hwSearchFilter;

		public HwSearchRequestor(AbstractContentProvider contentProvider, HardwareItemsFilter hwSearchFilter) {
			this.contentProvider = contentProvider;
			this.hwSearchFilter = hwSearchFilter;
		}

		public void acceptHardware(HardwareProperties hwConfig) {
			contentProvider.add(hwConfig, hwSearchFilter);
		}

	}

	public class HardwareItemsFilter extends ItemsFilter {

		public HardwareItemsFilter(List<Class<? extends HardwareProperties>> hwTypes) {
			super(new SearchPattern( SearchPattern.RULE_EXACT_MATCH | 
									 SearchPattern.RULE_PREFIX_MATCH | 
									 SearchPattern.RULE_PATTERN_MATCH | 
									 SearchPattern.RULE_CAMELCASE_MATCH));
		}
		@Override
		public boolean matchItem(Object item) {
			for (Class<? extends HardwareProperties> class1 : hwTypes) {
				if (class1.isInstance(item))
					return true;					
			}			
			return false;
		}

		@Override
		public boolean isConsistentItem(Object item) {
			return true;
		}
		
		@Override
		public String getPattern(){
			return "*";
		}
		

	}

	private static final String DIALOG_SETTINGS= "org.raspinloop.fmi.plugin.preferences.HardwareSelectionDialog";
			
	private ItemsFilter fFilter;

	private List<Class<? extends HardwareProperties>> hwTypes;
	
	public HardwareSelectionDialog(Shell shell, List<Class<? extends HardwareProperties>> hwTypes) {
		super(shell, false);
		this.hwTypes = hwTypes;
		setListLabelProvider(new HwLabelProvider());
		setDetailsLabelProvider(new HwDetailLabelProvider());
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {		
		return null;
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings= Activator.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);

		if (settings == null) {
			settings= Activator.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
		}

		return settings;
	}

	@Override
	protected IStatus validateItem(Object item) {
		if (item == null)
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "", null); //$NON-NLS-1$

		return Status.OK_STATUS;
	}

	@Override
	protected ItemsFilter createFilter() {
		fFilter= new HardwareItemsFilter(hwTypes);
		return fFilter;
	}

	@Override
	protected Comparator getItemsComparator() {		
		return new Comparator<HardwareProperties>() {

			@Override
			public int compare(HardwareProperties o1, HardwareProperties o2) {				
				return o1.getImplementationClassName().compareToIgnoreCase(o2.getImplementationClassName());
			}
		};
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter, IProgressMonitor progressMonitor) throws CoreException {
		HardwareItemsFilter hwSearchFilter= (HardwareItemsFilter) itemsFilter;
		HwSearchRequestor requestor= new HwSearchRequestor(contentProvider, hwSearchFilter);
		
		progressMonitor.setTaskName("seraching hardware");

		SearchHardware engine= new SearchHardware();
			engine.searchAllHardwareNames(					
					requestor,
					IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
					progressMonitor);
	}

	@Override
	public String getElementName(Object item) {
		if (item instanceof HardwareProperties ){
			HardwareProperties hwConfig = (HardwareProperties) item;
			return hwConfig.getComponentName();
		}
		return item.toString();
	}

}
