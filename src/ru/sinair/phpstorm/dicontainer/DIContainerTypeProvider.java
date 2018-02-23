package ru.sinair.phpstorm.dicontainer;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DIContainerTypeProvider extends CompletionContributor implements PhpTypeProvider3 {

    private static final String MAESTROPROG_CONTAINER_CONTAINER = "\\Maestroprog\\Container\\Container";
    private static final String MAESTROPROG_CONTAINER_ABSTRACT_BASIC_CONTAINER = "\\Maestroprog\\Container\\AbstractBasicContainer";

    @Override
    public char getKey() {
        return '\u0841';
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement psiElement) {
        if (isContainerGetMethod(psiElement)) {
            PhpPsiElement firstParam = (PhpPsiElement) getParameter((MethodReference) psiElement, 0);
            return getClass(firstParam);
        }
        return null;
    }

    private PhpType getClass(PhpPsiElement elem) {
        if (elem instanceof ClassConstantReference) {
            if (elem.getName() != null && elem.getName().equals("class")
                    && ((ClassConstantReference) elem).getClassReference() != null)
                return ((ClassConstantReference) elem).getClassReference().getType();
        }
        if (elem instanceof MethodReference) {
            if (elem.getName() != null && elem.getName().equals("className")
                    && ((MethodReference) elem).getClassReference() != null)
                return ((MethodReference) elem).getClassReference().getType();
        }

        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        Collection<PhpNamedElement> elements = new HashSet<>();

        return elements;
    }

    @Nullable
    private PsiElement getParameter(MethodReference methodRef, int index) {
        if (methodRef.getParameters().length >= index) {
            return methodRef.getParameters()[index];
        }
        return null;
    }

    private boolean isContainerGetMethod(PsiElement psiElement) {
        PhpIndex index = PhpIndex.getInstance(psiElement.getProject());

        if (psiElement instanceof MethodReference) {
            MethodReference referenceMethod = (MethodReference) psiElement;
            if (referenceMethod.getName() != null && referenceMethod.getName().equals("get")
                    && referenceMethod.getParameters().length == 1) {
                PhpExpression classReference = ((MethodReferenceImpl) psiElement).getClassReference();
                if (classReference != null && classReference.getType() != null) {
                    PhpType type = classReference.getType();
                    if (type.toStringResolved().contains(MAESTROPROG_CONTAINER_CONTAINER)) {
                        return true;
                    }
                    Collection<PhpClass> theClass = index.getClassesByFQN(type.toStringResolved());
                    for (PhpClass phpClass : theClass) {
                        for (PhpClass phpClass1 : phpClass.getSupers()) {
                            if (phpClass1.getFQN().equals(MAESTROPROG_CONTAINER_ABSTRACT_BASIC_CONTAINER)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
