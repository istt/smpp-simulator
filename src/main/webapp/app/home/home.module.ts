import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SmppSimulatorSharedModule } from 'app/shared';
import { HOME_ROUTE, HomeComponent, SmppMainComponent } from './';

@NgModule({
    imports: [SmppSimulatorSharedModule, RouterModule.forChild([HOME_ROUTE])],
    declarations: [HomeComponent, SmppMainComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class SmppSimulatorHomeModule {}
