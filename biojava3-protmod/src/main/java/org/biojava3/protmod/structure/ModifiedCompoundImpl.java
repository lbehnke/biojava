/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on Jun 5, 2010
 * Author: Jianjiong Gao 
 *
 */

package org.biojava3.protmod.structure;

import java.io.Serializable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.biojava3.protmod.ComponentType;
import org.biojava3.protmod.ModificationCategory;
import org.biojava3.protmod.ProteinModification;

/**
 * 
 * @author Jianjiong Gao
 * @since 3.0
 */
public class ModifiedCompoundImpl
implements ModifiedCompound, Serializable {
	private static final long serialVersionUID = -3809110608450334460L;

	public static final String newline = System.getProperty("line.separator");
	
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = -5345107617243742042L;
        
        private final String modificationId;
    	private final Set<StructureGroup> groups;
    	private final Set<StructureAtomLinkage> atomLinkages;
        SerializationProxy(ModifiedCompoundImpl actualObj) {
            this.modificationId = actualObj.modification.getId();
            this.groups = actualObj.groups;
            if (actualObj.atomLinkages == null)
            	this.atomLinkages = null;
            else
            	this.atomLinkages = actualObj.getAtomLinkages();
        }

        private Object readResolve() {
        	ProteinModification modification = ProteinModification.getById(modificationId);
        	if (atomLinkages==null) {
        		if (groups==null || groups.size()!=1) {
        			throw new IllegalStateException("Only one group is allowed without linkage.");
        		}
        		return new ModifiedCompoundImpl(modification, groups.iterator().next());
        	}
        	
            return new ModifiedCompoundImpl(modification, atomLinkages);
        }
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(java.io.ObjectInputStream stream)
    		throws java.io.InvalidObjectException {
        throw new java.io.InvalidObjectException("Proxy required");
    }
	
	private ProteinModification modification;
	private final Set<StructureGroup> groups;
	private Map<Set<StructureGroup>, Set<StructureAtomLinkage>> atomLinkages;
	
	/**
	 * Create a ModifiedCompoundImpl that has only one involved component.
	 * Use this constructor for a modified residue.
	 * @param modification {@link ProteinModification}.
	 * @param modifiedResidue modified group.
	 * @return a {@link ModifiedCompound}.
	 * @throws IllegalArgumentException if either argument is null.
	 */
	public ModifiedCompoundImpl (
			final ProteinModification modification,
			final StructureGroup modifiedResidue) {
		if (modification==null || modifiedResidue==null) {
			throw new IllegalArgumentException("Null argument(s)");
		}
		
		this.modification = modification;
		
		groups = new HashSet<StructureGroup>(1);
		groups.add(modifiedResidue);

		// is it possible that components be added by addLinkage later?
		atomLinkages = null;
	}
	
	/**
	 * 
	 * @param modification ProteinModification.
	 * @param linkages a collection of atom linkages.
	 * @see ProteinModification
	 * @see StructureAtomLinkage
	 */
	public ModifiedCompoundImpl(final ProteinModification modification,
			final Collection<StructureAtomLinkage> linkages) {
		if (modification==null) {
			throw new IllegalArgumentException("modification cannot be null");
		}

		if (linkages==null||linkages.isEmpty()) {
			throw new IllegalArgumentException("at least one linkage.");
		}
		
		this.modification = modification;
		
		this.groups = new HashSet<StructureGroup>();
				
		addAtomLinkages(linkages);
		
	}
	
	@Override
	public ProteinModification getModification() {
		if (modification.getCategory()!=ModificationCategory.UNDEFINED) {
			return modification;
		}
		
		int nRes = 0;
		for (StructureGroup group : groups) {
			if (group.getType() == ComponentType.AMINOACID) {
				nRes ++;
			}
		}
		
		ModificationCategory cat;
		switch (nRes) {
		case 0:
			return modification;
		case 1:
			cat = ModificationCategory.ATTACHMENT; break;
		case 2:
			cat = ModificationCategory.CROSS_LINK_2; break;
		case 3:
			cat = ModificationCategory.CROSS_LINK_3; break;
		case 4:
			cat = ModificationCategory.CROSS_LINK_4; break;
		case 5:
			cat = ModificationCategory.CROSS_LINK_5; break;
		case 6:
			cat = ModificationCategory.CROSS_LINK_6; break;
		case 7:
			cat = ModificationCategory.CROSS_LINK_7; break;
		default:
			cat = ModificationCategory.CROSS_LINK_8_OR_LARGE; break;
		}
		
		// TODO: name crashing?
		String newId = modification.getId() + "." + cat.name();
		
		ProteinModification mod = ProteinModification.getById(newId);
		if (mod != null) {
			return mod;
		}

		return ProteinModification.register(newId,
				cat, modification.getOccurrenceType(), modification.getCondition())
				.addKeywords(modification.getKeywords())
				.setDescription(modification.getDescription())
				.setFormula(modification.getFormula())
				.setPdbccId(modification.getPdbccId())
				.setPdbccName(modification.getPdbccName())
				.setPsimodId(modification.getPdbccName())
				.setPsimodName(modification.getPsimodName())
				.setResidId(modification.getResidId())
				.setResidName(modification.getResidName())
				.setSystematicName(modification.getSystematicName())
				.asModification();
	}
	
	@Override
	public Set<StructureGroup> getGroups() {
		return Collections.unmodifiableSet(groups);
	}
	
	@Override
	public Set<StructureGroup> getGroups(ComponentType type) {
		Set<StructureGroup> result = new HashSet<StructureGroup>();
		for (StructureGroup group : groups) {
			if (group.getType() == type) {
				result.add(group);
			}
		}
		return result;
	}

	@Override
	public Set<StructureAtomLinkage> getAtomLinkages() {
		if (atomLinkages==null) {
			return Collections.emptySet();
		} else {
			Set<StructureAtomLinkage> result = new HashSet<StructureAtomLinkage>();
			for (Set<StructureAtomLinkage> linkages : atomLinkages.values()) {
				result.addAll(linkages);
			}
			
			return result;
		}
	}

	@Override
	public boolean addAtomLinkage(StructureAtomLinkage linkage) {
		if (linkage==null) {
			throw new IllegalArgumentException("Null linkage");
		}
		
		Set<StructureGroup> gs = new HashSet<StructureGroup>(2);
		gs.add(linkage.getAtom1().getGroup());
		gs.add(linkage.getAtom2().getGroup());
		
		if (atomLinkages==null) {
			atomLinkages = new HashMap<Set<StructureGroup>, Set<StructureAtomLinkage>>();
		}

		Set<StructureAtomLinkage> linkages = atomLinkages.get(gs);
		if (linkages == null) {
			linkages = new HashSet<StructureAtomLinkage>();
			atomLinkages.put(gs, linkages);
			groups.addAll(gs); // it's possible of new groups
		};

		return linkages.add(linkage);
	}
	
	@Override
	public void addAtomLinkages(Collection<StructureAtomLinkage> linkages) {
		if (linkages==null) {
			throw new IllegalArgumentException("Null linkages");
		}
		
		for (StructureAtomLinkage link : linkages) {
			addAtomLinkage(link);
		}
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("Modification_");
		sb.append(modification.getId());
		ModificationCategory cat ;
		if (modification.getCategory()==ModificationCategory.UNDEFINED) {
			cat = getModification().getCategory();
		} else
			cat = modification.getCategory();
		sb.append("_");
		sb.append(cat.toString());
		return sb.toString();
	}
	
	
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Category: ");
		sb.append(getModification().getCategory());
		if (!modification.getKeywords().isEmpty()) {
			sb.append("; ");
			sb.append(modification.getKeywords());
		}
		sb.append("; Modification ID: ");
		sb.append(modification.getId());
		
		if (modification.getResidId()!=null) {
			sb.append("; RESID: ");
			sb.append(modification.getResidId());
			sb.append(" [");
			sb.append(modification.getResidName());
			sb.append(']');
		}
		
		sb.append(" | ");
		
		if (atomLinkages==null) {
			for (StructureGroup group : groups) {
				sb.append(group);
				sb.append(" | ");
			}
		} else {
			for (Set<StructureAtomLinkage> linkages : atomLinkages.values()) {
				for (StructureAtomLinkage linkage : linkages) {
					sb.append(linkage);
					sb.append(" | ");
				}
			}
		}

		return sb.toString();
	}
	
	/**
	 * @return true if same modification and same components; false, otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ModifiedCompound)) {
			return false;
		}
		
		ModifiedCompound mci = (ModifiedCompound)obj;
		if (mci.getModification() != modification) {
			return false;
		}
		
		if (!groups.equals(mci.getGroups())) {
			return false;
		}
		
		return true;
		
		// Do not need to consider linkage, since they can be determined by
		// modification and groups.
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 32 + modification.hashCode();
		
		int sum = 0;
		for (StructureGroup group : groups) {
			sum += group.hashCode();
		}
		
		result = result * 32 + sum;
		
		return result;
	}
}
