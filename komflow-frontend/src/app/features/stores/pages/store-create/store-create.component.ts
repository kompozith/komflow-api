import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MaterialModule } from 'src/app/material.module';
import { CommonModule } from '@angular/common';
import { StoreManagementFacade } from '../../services/store-management.facade';
import { CreateStoreRequest } from '../../models/store';
import { BrandManagementFacade } from '../../../brands/services/brand-management.facade';
import { Brand } from '../../../brands/models/brand';

@Component({
  selector: 'app-store-create',
  templateUrl: './store-create.component.html',
  styleUrls: ['./store-create.component.scss'],
  imports: [MaterialModule, ReactiveFormsModule, CommonModule],
})
export class StoreCreateComponent implements OnInit {
  isEditMode = false;
  storeForm: FormGroup;
  brandOptions: Brand[] = [];
  isLoadingBrands = false;

  constructor(
    private router: Router,
    private storeManagementFacade: StoreManagementFacade,
    private brandManagementFacade: BrandManagementFacade,
    private snackBar: MatSnackBar
  ) {
    this.storeForm = new FormGroup({
      name: new FormControl('', [Validators.required, Validators.minLength(2)]),
      description: new FormControl(''),
      phone: new FormControl('', [Validators.required]),
      brandId: new FormControl('', [Validators.required]),
      // Address fields
      streetAddress: new FormControl('', [Validators.required]),
      city: new FormControl('', [Validators.required]),
      region: new FormControl('', [Validators.required]),
      landmark: new FormControl(''),
      instructions: new FormControl(''),
    });
  }

  ngOnInit(): void {
    this.loadBrands();
  }

  private loadBrands(): void {
    this.isLoadingBrands = true;
    this.brandManagementFacade.getBrands({ size: 1000 }).subscribe({
      next: (response) => {
        this.brandOptions = response.content;
        this.isLoadingBrands = false;
      },
      error: (error) => {
        console.error('Error loading brands:', error);
        this.brandOptions = [];
        this.isLoadingBrands = false;
      }
    });
  }

  getBack(): void {
    this.router.navigate(['/stores']);
  }

  onSubmit(): void {
    if (this.storeForm.valid) {
      const formValue = this.storeForm.value;
      const storeData: CreateStoreRequest = {
        name: formValue.name,
        description: formValue.description || undefined,
        phone: formValue.phone,
        brandId: formValue.brandId,
        address: {
          streetAddress: formValue.streetAddress,
          city: formValue.city,
          region: formValue.region,
          postalCode: '',
          country: '',
          landmark: formValue.landmark || undefined,
          instructions: formValue.instructions || undefined,
        },
      };

      this.storeManagementFacade.createStore(storeData).subscribe({
        next: (store) => {
          this.snackBar.open('Store created successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/stores']);
        },
        error: (error) => {
          console.error('Error creating store:', error);
          this.snackBar.open('Error creating store', 'Close', { duration: 3000 });
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.storeForm.controls).forEach(key => {
      const control = this.storeForm.get(key);
      control?.markAsTouched();
    });
  }
}