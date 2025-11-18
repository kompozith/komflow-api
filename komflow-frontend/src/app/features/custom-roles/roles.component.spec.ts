import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomRolesComponent } from './roles.component';

describe('CustomRolesComponent', () => {
  let component: CustomRolesComponent;
  let fixture: ComponentFixture<CustomRolesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomRolesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CustomRolesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
