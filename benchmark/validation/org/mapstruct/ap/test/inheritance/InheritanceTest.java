/**
 * Copyright MapStruct Authors.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.mapstruct.ap.test.inheritance;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.ap.testutil.IssueKey;
import org.mapstruct.ap.testutil.WithClasses;
import org.mapstruct.ap.testutil.runner.AnnotationProcessorTestRunner;


/**
 * Test for propagation of attributes inherited from super types.
 *
 * @author Gunnar Morling
 */
@WithClasses({ SourceBase.class, SourceExt.class, TargetBase.class, TargetExt.class, SourceTargetMapper.class })
@RunWith(AnnotationProcessorTestRunner.class)
public class InheritanceTest {
    @Test
    @IssueKey("17")
    public void shouldMapAttributeFromSuperType() {
        SourceExt source = createSource();
        TargetExt target = SourceTargetMapper.INSTANCE.sourceToTarget(source);
        assertResult(target);
    }

    @Test
    @IssueKey("19")
    public void shouldMapAttributeFromSuperTypeUsingTargetParameter() {
        SourceExt source = createSource();
        TargetExt target = new TargetExt();
        SourceTargetMapper.INSTANCE.sourceToTargetWithTargetParameter(target, source);
        assertResult(target);
    }

    @Test
    @IssueKey("19")
    public void shouldMapAttributeFromSuperTypeUsingReturnedTargetParameter() {
        SourceExt source = createSource();
        TargetExt target = new TargetExt();
        TargetBase result = SourceTargetMapper.INSTANCE.sourceToTargetWithTargetParameterAndReturn(source, target);
        assertThat(target).isSameAs(result);
        assertResult(target);
    }

    @Test
    @IssueKey("17")
    public void shouldReverseMapAttributeFromSuperType() {
        TargetExt target = new TargetExt();
        target.setFoo(42L);
        target.publicFoo = 52L;
        target.setBar(23);
        SourceExt source = SourceTargetMapper.INSTANCE.targetToSource(target);
        assertThat(source).isNotNull();
        assertThat(source.getFoo()).isEqualTo(42);
        assertThat(source.publicFoo).isEqualTo(52);
        assertThat(source.getBar()).isEqualTo(Long.valueOf(23));
    }
}
