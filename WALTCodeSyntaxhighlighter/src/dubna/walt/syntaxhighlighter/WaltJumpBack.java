/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dubna.walt.syntaxhighlighter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Edit",
        id = "dubna.walt.syntaxhighlighter.WaltJumpBack"
)
@ActionRegistration(
        iconBase = "dubna/walt/syntaxhighlighter/bar_icon_prev.png",
        displayName = "#CTL_WaltJumpBack"
)
@ActionReferences({
    @ActionReference(path = "Menu/GoTo", position = 2900, separatorAfter = 2950)
    ,
  @ActionReference(path = "Toolbars/UndoRedo", position = 300)
    ,
  @ActionReference(path = "Shortcuts", name = "DO-BACK_SPACE")
})
@Messages("CTL_WaltJumpBack=WaltJumpBack")
public final class WaltJumpBack implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO implement action body
    }
}
